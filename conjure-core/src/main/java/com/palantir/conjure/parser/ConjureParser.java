/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.palantir.conjure.exceptions.ConjureRuntimeException;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.logsafe.exceptions.SafeIllegalStateException;
import com.palantir.logsafe.exceptions.SafeUncheckedIoException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

public final class ConjureParser {

    private static final ObjectMapper MAPPER = createConjureParserObjectMapper();

    private static String getErrorMessage(File file, Optional<File> importedFrom) {
        String message = "Import not found: " + file.getAbsolutePath();
        return importedFrom
                .map(importedFromFile -> message + " (imported from " + importedFromFile.getAbsolutePath() + ")")
                .orElse(message);
    }

    public static class ImportNotFoundException extends RuntimeException {
        public ImportNotFoundException(File file) {
            this(file, Optional.empty());
        }

        public ImportNotFoundException(File file, Optional<File> importedFrom) {
            super(getErrorMessage(file, importedFrom));
        }
    }

    public static class CyclicImportException extends RuntimeException {
        public CyclicImportException(String path) {
            super("Cyclic conjure imports are not allowed: " + path);
        }
    }

    private ConjureParser() {}

    /** Deserializes a {@link ConjureSourceFile} from its YAML representation in the given file. */
    public static ConjureSourceFile parse(File file) {
        CachingParser parser = new CachingParser();
        return parser.parse(file);
    }

    /**
     * Parse file {@literal &} all imports (breadth-first).
     */
    public static Map<String, AnnotatedConjureSourceFile> parseAnnotated(File file) {
        return parseAnnotated(ImmutableList.of(file));
    }

    /**
     * Parse all files {@literal &} imports (breadth-first).
     */
    public static Map<String, AnnotatedConjureSourceFile> parseAnnotated(Collection<File> files) {
        CachingParser parser = new CachingParser();

        Map<String, AnnotatedConjureSourceFile> parsed = new HashMap<>();
        Queue<FileWithProvenance> toProcess =
                new ArrayDeque<>(files.stream().map(FileWithProvenance::of).collect(Collectors.toList()));

        while (!toProcess.isEmpty()) {
            FileWithProvenance nextFileWithProvenance = toProcess.poll();
            File nextFile;
            try {
                nextFile = nextFileWithProvenance.file().getCanonicalFile();
            } catch (IOException e) {
                throw new SafeUncheckedIoException("Couldn't canonicalize file path", e);
            }
            String key = nextFile.getAbsolutePath();

            if (!parsed.containsKey(key)) {
                AnnotatedConjureSourceFile annotatedConjureSourceFile =
                        parseSingleFile(parser, nextFile, nextFileWithProvenance.importedFrom());
                parsed.put(key, annotatedConjureSourceFile);

                // Add all imports as files to be parsed
                annotatedConjureSourceFile.importProviders().values().stream()
                        .map(File::new)
                        .forEach(file -> toProcess.add(FileWithProvenance.of(file, nextFile)));
            }
        }

        return parsed;
    }

    private static AnnotatedConjureSourceFile parseSingleFile(
            CachingParser parser, File file, Optional<File> importedFrom) {
        ConjureSourceFile parsed = parser.parse(file, importedFrom);

        return AnnotatedConjureSourceFile.builder()
                .conjureSourceFile(parsed)
                .sourceFile(file)
                // Hoist imports
                .importProviders(parsed.types().conjureImports().entrySet().stream()
                        .collect(Collectors.toMap(
                                Entry::getKey,
                                entry -> entry.getValue()
                                        .absoluteFile()
                                        .orElseThrow(() -> new SafeIllegalStateException(
                                                "Absolute file MUST be resolved as part of parsing stage"))
                                        .getAbsolutePath())))
                .build();
    }

    private static final class CachingParser {
        // From absolute path to the parsed file
        private final Map<String, ConjureSourceFile> cache;

        private CachingParser() {
            this.cache = new HashMap<>();
        }

        ConjureSourceFile parse(File file) {
            return parse(file, Optional.empty());
        }

        ConjureSourceFile parse(File file, Optional<File> importedFrom) {
            // HashMap.computeIfAbsent does not work with recursion; the size of the map gets corrupted,
            // and if the map gets resized during the recursion, some of the new nodes can be put in wrong
            // buckets. Therefore don't use computeIfAbsent in parse/parseInternal
            // See https://bugs.java.com/view_bug.do?bug_id=JDK-8071667
            ConjureSourceFile result = cache.get(file.getAbsolutePath());
            if (result != null) {
                return result;
            }

            result = parseInternal(file, importedFrom);
            cache.put(file.getAbsolutePath(), result);
            return result;
        }

        private ConjureSourceFile parseInternal(File file, Optional<File> importedFrom) {
            if (!Files.exists(file.toPath())) {
                throw new ImportNotFoundException(file, importedFrom);
            }

            try {
                ConjureSourceFile definition = MAPPER.readValue(file, ConjureSourceFile.class);

                // For ease of book-keeping, resolve the import paths here
                Path baseDir = file.toPath().getParent();
                return ConjureSourceFile.builder()
                        .from(definition)
                        .types(TypesDefinition.builder()
                                .from(definition.types())
                                .conjureImports(definition.types().conjureImports().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Entry::getKey,
                                                // Resolve absolute path to ensure we don't need to track this anymore
                                                conjureImport ->
                                                        conjureImport.getValue().resolve(baseDir))))
                                .build())
                        .build();
            } catch (IOException e) {
                throw new ConjureRuntimeException(String.format("Error while parsing %s:", file), e);
            }
        }
    }

    @VisibleForTesting
    static ObjectMapper createConjureParserObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .registerModule(new Jdk8Module())
                .setAnnotationIntrospector(AnnotationIntrospector.pair(
                        new KebabCaseEnforcingAnnotationInspector(), // needs to come first.
                        new JacksonAnnotationIntrospector()));
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        return mapper;
    }
}

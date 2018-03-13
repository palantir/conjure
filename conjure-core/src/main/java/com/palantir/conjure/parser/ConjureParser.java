/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.reference.ConjureImports;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ConjureParser {

    private static final ObjectMapper MAPPER = createConjureParserObjectMapper();

    public static class ImportNotFoundException extends RuntimeException {
        public ImportNotFoundException(File file) {
            super("Import not found: " + file.getAbsolutePath());
        }
    }

    public static class CyclicImportException extends RuntimeException {
        public CyclicImportException(String path) {
            super("Cyclic conjure imports are not allowed: " + path);
        }
    }

    private ConjureParser() {}

    /** Deserializes a {@link ConjureDefinition} from its YAML representation in the given file. */
    public static ConjureDefinition parse(File file) {
        RecursiveParser parser = new RecursiveParser();
        ConjureDefinition conjureDef = parser.parse(file);
        ConjureMetrics.recordMetrics(conjureDef);
        return conjureDef;
    }

    private static final class RecursiveParser {
        private final Map<String, ConjureDefinition> cache;
        private final Set<String> currentDepthFirstPath;

        private RecursiveParser() {
            this.cache = new HashMap<>();
            this.currentDepthFirstPath = new LinkedHashSet<>(); // maintain order so we can print the cycle
        }

        ConjureDefinition parse(File file) {
            // HashMap.computeIfAbsent does not work with recursion; the size of the map gets corrupted,
            // and if the map gets resized during the recursion, some of the new nodes can be put in wrong
            // buckets. Therefore don't use computeIfAbsent in parse/parseInternal
            // See https://bugs.java.com/view_bug.do?bug_id=JDK-8071667
            ConjureDefinition result = cache.get(file.getAbsolutePath());
            if (result != null) {
                return result;
            }

            if (!currentDepthFirstPath.add(file.getAbsolutePath())) {
                String cycle = currentDepthFirstPath.stream().reduce("", (left, right) -> left + " -> " + right)
                        + " -> " + file.getAbsolutePath();
                throw new CyclicImportException(cycle);
            }

            result = parseInternal(file);
            cache.put(file.getAbsolutePath(), result);
            return result;
        }

        private ConjureDefinition parseInternal(File file) {
            // Note(rfink): The mechanism of parsing the ConjureDefinition and the imports separately isn't pretty,
            // but it's better than the previous implementation where ConjureImports types were passed around all
            // over the place. Main obstacle to simpler parsing is that Jackson parsers don't have context, i.e., it's
            // impossible to know the base-path w.r.t. which the imported file is declared.
            if (!Files.exists(file.toPath())) {
                throw new ImportNotFoundException(file);
            }

            try {
                ConjureDefinition definition = MAPPER.readValue(file, ConjureDefinition.class);
                Map<Namespace, ConjureImports> imports =
                        parseImports(definition.types().conjureImports(), file.toPath().getParent());
                return ConjureDefinition.builder()
                        .from(definition)
                        .types(TypesDefinition.builder()
                                .from(definition.types())
                                .conjureImports(imports)
                                .build())
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Replaces the (typically empty) ImportedTypes object for each namespace by an object with inlined/populated
         * {@link ConjureImports#conjure()} imported definitions}.
         */
        private Map<Namespace, ConjureImports> parseImports(
                Map<Namespace, ConjureImports> declaredImports, Path baseDir) {
            Map<Namespace, ConjureImports> parsedImports = Maps.newHashMap();
            for (Namespace namespace : declaredImports.keySet()) {
                String importedFile = declaredImports.get(namespace).file();
                ConjureDefinition importedConjure = parse(baseDir.resolve(importedFile).toFile());
                parsedImports.put(namespace, ConjureImports.withResolvedImports(importedFile, importedConjure));
            }
            return parsedImports;
        }

    }

    @VisibleForTesting
    static ObjectMapper createConjureParserObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .registerModule(new Jdk8Module())
                .setAnnotationIntrospector(
                        AnnotationIntrospector.pair(
                                new KebabCaseEnforcingAnnotationInspector(), // needs to come first.
                                new JacksonAnnotationIntrospector()));
        mapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        return mapper;
    }
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.types.ObjectsDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.Namespace;
import com.palantir.conjure.defs.types.reference.ImportedTypes;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

public final class Conjure {

    private static final ObjectMapper MAPPER = createConjureParserObjectMapper();

    private Conjure() {}

    /**
     * Deserializes a {@link ConjureDefinition} from its YAML representation in the given file. Unlike {@link
     * #parse(InputStream)} and {@link #parse(String)}, this variant deserializes {@link ImportedTypes#file refernced
     * conjure imports} into {@link ImportedTypes#importedTypes}
     */
    public static ConjureDefinition parse(File file) {
        // Note(rfink): The mechanism of parsing the ConjureDefinition and the imports separately isn't pretty, but it's
        // better than the previous implementation where ConjureImports objects were passed around all over the place.
        // Main obstacle to simpler parsing is that Jackson parsers don't have context, i.e., it's impossible to know
        // the base-path w.r.t. which the imported file is declared.
        try {
            ConjureDefinition definition = MAPPER.readValue(file, ConjureDefinition.class);
            // TODO(rfink): https://github.palantir.build/foundry/conjure/issues/367
            // This is a recursive invocation of Conjure#parse which is going to fail with a stack overflow error
            // when imports are circular. Should detect circles and fail with a more helpful error message.
            Map<Namespace, ImportedTypes> imports =
                    Conjure.parseImports(definition.types().conjureImports(), file.toPath().getParent());
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
     * Like {@link #parse(File)}, but does not deserialize and inline {@link ImportedTypes#importedTypes conjure
     * imports}.
     */
    public static ConjureDefinition parse(InputStream stream) {
        try {
            return MAPPER.readValue(stream, ConjureDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Like {@link #parse(File)}, but does not deserialize and inline {@link ImportedTypes#importedTypes conjure
     * imports}.
     */
    public static ConjureDefinition parse(String string) {
        try {
            return MAPPER.readValue(string, ConjureDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Replaces the (typically empty) ImportedTypes object for each namespace by an object with inlined/populated {@link
     * ImportedTypes#importedTypes mported types}.
     */
    private static Map<Namespace, ImportedTypes> parseImports(
            Map<Namespace, ImportedTypes> declaredImports, Path baseDir) {
        Map<Namespace, ImportedTypes> parsedImports = Maps.newHashMap();
        for (Namespace namespace : declaredImports.keySet()) {
            String importedFile = declaredImports.get(namespace).file();
            try {
                ObjectsDefinition importedObjects =
                        Conjure.parse(baseDir.resolve(importedFile).toFile()).types().definitions();
                parsedImports.put(namespace, ImportedTypes.withResolvedImports(importedFile, importedObjects));
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to open imported Conjure definition: + " + importedFile, e);
            }
        }
        return parsedImports;
    }

    @VisibleForTesting
    static ObjectMapper createConjureParserObjectMapper() {
        return new ObjectMapper(new YAMLFactory())
                .registerModule(new Jdk8Module())
                .setAnnotationIntrospector(
                        AnnotationIntrospector.pair(
                                new KebabCaseEnforcingAnnotationInspector(), // needs to come first.
                                new JacksonAnnotationIntrospector()));
    }
}

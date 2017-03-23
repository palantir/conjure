/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface TypeGenerator {

    default Set<JavaFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports) {
        TypesDefinition types = conjureDefinition.types();
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        imports,
                        types.definitions().defaultPackage(),
                        type.getKey(),
                        type.getValue()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    default void emit(ConjureDefinition conjureDefinition, ConjureImports imports, File outputDir) {
        generate(conjureDefinition, imports).forEach(f -> {
            try {
                f.writeTo(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    JavaFile generateType(
            TypesDefinition allTypes,
            ConjureImports importedTypes,
            String defaultPackage,
            String typeName,
            BaseObjectTypeDefinition typeDef);

}

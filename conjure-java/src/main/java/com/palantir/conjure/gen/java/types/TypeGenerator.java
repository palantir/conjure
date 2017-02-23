/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.Throwables;
import com.palantir.conjure.defs.Conjure;
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

    default void emit(ConjureDefinition conjureDefinition, File outputDir) {
        generate(conjureDefinition).forEach(file -> {
            try {
                file.writeTo(outputDir);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });
    }

    default Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        TypesDefinition types = conjureDefinition.types();
        ConjureImports conjureImports = Conjure.parseTypesFromConjureImports(conjureDefinition);
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        conjureImports,
                        types.definitions().defaultPackage(),
                        type.getKey(),
                        type.getValue()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    JavaFile generateType(
            TypesDefinition allTypes,
            ConjureImports importedTypes,
            String defaultPackage,
            String typeName,
            BaseObjectTypeDefinition typeDef);

}

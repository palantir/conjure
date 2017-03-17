/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public interface TypeGenerator {

    // TODO(rfink): The API is a bit silly and needs refactoring: Why on earth do I need to pass in a baseDir here?
    default void emit(ConjureDefinition conjureDefinition, Path baseDir, File outputDir) {
        generate(conjureDefinition, baseDir).forEach(file -> {
            try {
                file.writeTo(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    default Set<JavaFile> generate(ConjureDefinition conjureDefinition, Path baseDir) {
        TypesDefinition types = conjureDefinition.types();
        ConjureImports conjureImports = Conjure.parseTypesFromConjureImports(conjureDefinition, baseDir);
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

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.google.common.base.Throwables;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public interface TypeGenerator {

    default void emit(TypesDefinition types, Settings settings, File outputDir) {
        generate(types, settings).forEach(file -> {
            try {
                file.writeTo(outputDir);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });
    }

    default Set<JavaFile> generate(TypesDefinition types, Settings settings) {
        TypeMapper typeMapper = new TypeMapper(types, settings.optionalTypeStrategy());
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        settings,
                        typeMapper,
                        types.definitions().defaultPackage(),
                        type.getKey(),
                        type.getValue()))
                .collect(Collectors.toSet());
    }

    JavaFile generateType(TypesDefinition types, Settings settings, TypeMapper typeMapper, String defaultPackage,
            String typeName, ObjectTypeDefinition typeDef);

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.google.common.base.Throwables;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public interface TypeGenerator {

    default void emit(TypesDefinition types, File outputDir) {
        generate(types).forEach(file -> {
            try {
                file.writeTo(outputDir);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });
    }

    default Set<TypescriptFile> generate(TypesDefinition types) {
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        types.definitions().defaultPackage(),
                        type.getKey(),
                        type.getValue()))
                .filter(x -> x != null) // TODO(melliot) consider optional
                .collect(Collectors.toSet());
    }

    TypescriptFile generateType(TypesDefinition types, String defaultPackage,
            String typeName, BaseObjectTypeDefinition typeDef);

}

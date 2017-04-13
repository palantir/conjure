/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.collect.Lists;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ConjureImports;
import com.palantir.conjure.gen.java.util.Goethe;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TypeGenerator {

    default Set<JavaFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports) {
        TypesDefinition types = conjureDefinition.types();
        return types.definitions().objects().entrySet().stream().map(
                type -> generateType(
                        types,
                        imports,
                        types.definitions().defaultConjurePackage(),
                        type.getKey(),
                        type.getValue()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    default List<Path> emit(ConjureDefinition conjureDefinition, ConjureImports imports, File outputDir) {
        List<Path> emittedPaths = Lists.newArrayList();
        generate(conjureDefinition, imports).forEach(f -> {
            Path emittedPath = Goethe.formatAndEmit(f, outputDir.toPath());
            emittedPaths.add(emittedPath);
        });
        return emittedPaths;
    }

    JavaFile generateType(
            TypesDefinition allTypes,
            ConjureImports importedTypes,
            Optional<ConjurePackage> defaultPackage,
            TypeName typeName,
            BaseObjectTypeDefinition typeDef);
}

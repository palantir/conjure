/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ObjectsDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.gen.java.util.Goethe;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface TypeGenerator {

    default Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        TypesDefinition types = conjureDefinition.types();
        ObjectsDefinition objectsDefinition = types.definitions();
        Set<JavaFile> files = Sets.newLinkedHashSet();

        // Generate java files for object definitions
        objectsDefinition.types().stream().map(
                type -> generateObjectType(types, type))
                .forEach(files::add);

        // Generate java files for error definitions
        generateErrorTypes(types, objectsDefinition.errors())
                .forEach(files::add);

        return files;
    }

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    default List<Path> emit(ConjureDefinition conjureDefinition, File outputDir) {
        List<Path> emittedPaths = Lists.newArrayList();
        generate(conjureDefinition).forEach(f -> {
            Path emittedPath = Goethe.formatAndEmit(f, outputDir.toPath());
            emittedPaths.add(emittedPath);
        });
        return emittedPaths;
    }

    JavaFile generateObjectType(
            TypesDefinition allTypes,
            BaseObjectTypeDefinition typeDef);

    Set<JavaFile> generateErrorTypes(
            TypesDefinition allTypes,
            List<ErrorTypeDefinition> errorTypeNameToDef);
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.gen.java.util.Goethe;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public interface TypeGenerator {

    default Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        Set<JavaFile> files = Sets.newLinkedHashSet();

        // Generate java files for type definitions
        generateTypes(conjureDefinition.types()).forEach(files::add);

        // Generate java files for error definitions
        generateErrors(conjureDefinition.types(), conjureDefinition.errors()).forEach(files::add);

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

    Set<JavaFile> generateTypes(List<TypeDefinition> types);

    Set<JavaFile> generateErrors(List<TypeDefinition> types, List<ErrorTypeDefinition> errors);
}

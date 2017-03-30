/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.gen.java.types.TypeGenerator;
import com.palantir.conjure.gen.java.util.Goethe;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public final class ConjureJavaServiceAndTypeGenerator {

    private final ServiceGenerator serviceGenerator;
    private final TypeGenerator typeGenerator;

    public ConjureJavaServiceAndTypeGenerator(ServiceGenerator serviceGenerator, TypeGenerator typeGenerator) {
        this.serviceGenerator = serviceGenerator;
        this.typeGenerator = typeGenerator;
    }

    /**
     * Generates and returns all services and types of the given conjure definition, using the instance's service and
     * type generators.
     */
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports) {
        return Sets.union(
                serviceGenerator.generate(conjureDefinition, imports),
                typeGenerator.generate(conjureDefinition, imports));
    }

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    public List<Path> emit(ConjureDefinition conjureDefinition, ConjureImports imports, File outputDir) {
        List<Path> emittedPaths = Lists.newArrayList();
        generate(conjureDefinition, imports).forEach(f -> {
            Path emittedPath = Goethe.formatAndEmit(f, outputDir.toPath());
            emittedPaths.add(emittedPath);
        });
        return emittedPaths;
    }
}

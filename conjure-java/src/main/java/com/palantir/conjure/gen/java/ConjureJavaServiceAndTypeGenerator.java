/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.gen.java.types.TypeGenerator;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
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
    public void emit(ConjureDefinition conjureDefinition, ConjureImports imports, File outputDir) {
        generate(conjureDefinition, imports).forEach(f -> {
            try {
                f.writeTo(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

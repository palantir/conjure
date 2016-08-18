/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
import com.palantir.conjure.gen.typescript.staticgen.StaticGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import com.palantir.conjure.gen.typescript.types.TypeGenerator;
import java.io.File;

public final class ConjureTypescriptClientGenerator {

    private final ServiceGenerator serviceGenerator;
    private final StaticGenerator staticGenerator;
    private final TypeGenerator typeGenerator;

    public ConjureTypescriptClientGenerator(ServiceGenerator serviceGenerator, StaticGenerator staticGenerator,
            TypeGenerator typeGenerator) {
        this.serviceGenerator = serviceGenerator;
        this.staticGenerator = staticGenerator;
        this.typeGenerator = typeGenerator;
    }

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    public void emit(ConjureDefinition conjureDefinition, File outputDir) {
        serviceGenerator.emit(conjureDefinition, outputDir);
        typeGenerator.emit(conjureDefinition.types(), outputDir);
        staticGenerator.emit(conjureDefinition, outputDir);
    }

    public static void main(String[] args) {
        String outputAbsPath = args[0];
        File folder = new File(outputAbsPath);
        if (!folder.exists()) {
            throw new RuntimeException("First arg should be a folder that exists");
        }

        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/test-service.conjure"));
        ConjureTypescriptClientGenerator generator = new ConjureTypescriptClientGenerator(
                new DefaultServiceGenerator(),
                new StaticGenerator(),
                new DefaultTypeGenerator());
        generator.emit(conjure, folder);
    }
}

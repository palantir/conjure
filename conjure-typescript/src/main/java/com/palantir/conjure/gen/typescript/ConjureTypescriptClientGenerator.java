/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import com.palantir.conjure.gen.typescript.types.TypeGenerator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ConjureTypescriptClientGenerator {

    private final ServiceGenerator serviceGenerator;
    private final TypeGenerator typeGenerator;

    public ConjureTypescriptClientGenerator(ServiceGenerator serviceGenerator,
            TypeGenerator typeGenerator) {
        this.serviceGenerator = serviceGenerator;
        this.typeGenerator = typeGenerator;
    }

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    public void emit(Set<ConjureDefinition> conjureDefinitions, File outputDir) {
        conjureDefinitions.forEach(conjureDefinition -> {
            serviceGenerator.emit(conjureDefinition, outputDir);
            typeGenerator.emit(conjureDefinition.types(), outputDir);
        });

        // write index file
        Set<ExportStatement> serviceExports = conjureDefinitions.stream()
                .flatMap(conjureDef -> serviceGenerator.generateExports(conjureDef).stream())
                .collect(Collectors.toSet());
        Set<ExportStatement> typeExports = conjureDefinitions.stream()
                .flatMap(conjureDef -> typeGenerator.generateExports(conjureDef.types()).stream())
                .collect(Collectors.toSet());

        DuplicateExportHandler duplicateHandler = new DropDuplicateExportHandler();

        List<ExportStatement> sortedExports = duplicateHandler.handleDuplicates(Sets.union(serviceExports, typeExports))
                .stream().sorted().collect(Collectors.toList());

        TypescriptFile indexFile = TypescriptFile.builder()
                .emittables(sortedExports)
                .name("index")
                .parentFolderPath("")
                .build();

        try {
            indexFile.writeTo(outputDir);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            throw new RuntimeException("Expected 2 args, got " + args.length);
        }
        File conjureFile = new File(args[0]);
        File outputDir = new File(args[1]);
        ConjureDefinition definition = Conjure.parse(conjureFile);
        ConjureTypescriptClientGenerator generator = new ConjureTypescriptClientGenerator(new DefaultServiceGenerator(),
                new DefaultTypeGenerator());
        generator.emit(ImmutableSet.of(definition), outputDir);
    }
}

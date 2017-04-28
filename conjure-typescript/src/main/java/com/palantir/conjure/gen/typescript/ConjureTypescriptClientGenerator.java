/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
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
     * <p>
     * Takes in a list of {@link ConjureDefinition}s in order to produce a single index file.
     */
    public void emit(List<ConjureDefinition> conjureDefinitions, File outputDir) {
        conjureDefinitions.forEach(definition -> {
            serviceGenerator.emit(definition, outputDir);
            typeGenerator.emit(definition.types(), outputDir);
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
            throw new RuntimeException(e);
        }
    }

}

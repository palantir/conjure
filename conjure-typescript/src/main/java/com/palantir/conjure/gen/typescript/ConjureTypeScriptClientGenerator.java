/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
import com.palantir.conjure.gen.typescript.types.TypeGenerator;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class ConjureTypeScriptClientGenerator {

    private final ServiceGenerator serviceGenerator;
    private final TypeGenerator typeGenerator;

    public ConjureTypeScriptClientGenerator(ServiceGenerator serviceGenerator,
            TypeGenerator typeGenerator) {
        this.serviceGenerator = serviceGenerator;
        this.typeGenerator = typeGenerator;
    }

    /**
     * Generates and emits to the given output directory all services and types of the provided conjure definitions,
     * using the instance's service and type generators.
     * <p>
     * Multiple directories, each corresponding to a logical TypeScript module, may be produced. Each directory contains
     * the services and types in the same {@link ConjurePackage}, across all provided conjure definitions, as well as an
     * index file at the directory root.
     */
    public void emit(List<ConjureDefinition> conjureDefinitions, File outputDir) {
        conjureDefinitions.forEach(definition -> {
            serviceGenerator.emit(definition, outputDir);
            typeGenerator.emit(definition.types(), outputDir);
        });

        // write index file for each module
        Multimap<ConjurePackage, ExportStatement> allExports = HashMultimap.create();
        conjureDefinitions.forEach(conjureDef -> serviceGenerator.generateExports(conjureDef)
                .forEach((conjurePackage, exports) -> allExports.putAll(conjurePackage, exports)));
        conjureDefinitions.forEach(conjureDef -> typeGenerator.generateExports(conjureDef.types())
                .forEach((conjurePackage, exports) -> allExports.putAll(conjurePackage, exports)));

        allExports.asMap().forEach((conjurePackage, exports) -> {
            TypescriptFile indexFile = TypescriptFile.builder()
                    .emittables(exports.stream().sorted().collect(Collectors.toList()))
                    .name("index")
                    .parentFolderPath(GenerationUtils.packageToScopeAndModule(conjurePackage))
                    .build();
            try {
                indexFile.writeTo(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

}

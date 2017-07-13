/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.services.ServiceGenerator;
import com.palantir.conjure.gen.typescript.types.TypeGenerator;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ConjureTypeScriptClientGenerator {

    public static final String CONJURE_FE_LIB = "@foundry/conjure-fe-lib";
    public static final String CONJURE_FE_LIB_VERSION = "^1.0.1";

    private static final ObjectMapper prettyPrintingMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

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
    public void emit(List<ConjureDefinition> conjureDefinitions, String projectVersion, File outputDir) {

        Set<TypescriptFile> allFiles = Sets.newHashSet();
        conjureDefinitions.forEach(definition -> {
            allFiles.addAll(serviceGenerator.generate(definition));
            allFiles.addAll(typeGenerator.generate(definition.types()));
        });
        allFiles.forEach(f -> emit(f, outputDir));

        // write package.json for each module
        Map<String, Collection<TypescriptFile>> filesByScopeAndModule = Multimaps
                .index(allFiles, TypescriptFile::parentFolderPath)
                .asMap();
        Map<String, Set<String>> externalImportsByScopeAndModule = filesByScopeAndModule
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> externalImports(entry.getValue())));
        externalImportsByScopeAndModule.forEach((scopeAndModule, externalImports) -> {
            String formattedProjectVersion = "^" + projectVersion;
            Map<String, String> dependencies = externalImports.stream()
                    .collect(Collectors.toMap(Function.identity(), ignored -> formattedProjectVersion));
            PackageJson packageJson = PackageJson.builder()
                    .author("Palantir Technologies, Inc.")
                    .putPeerDependencies(CONJURE_FE_LIB, CONJURE_FE_LIB_VERSION)
                    .version(projectVersion)
                    .description("Conjure definitions for " + scopeAndModule)
                    .name(scopeAndModule)
                    .dependencies(dependencies)
                    .putDevDependencies(CONJURE_FE_LIB, CONJURE_FE_LIB_VERSION)
                    .build();
            File packageJsonFile = Paths.get(outputDir.getAbsolutePath(), scopeAndModule, "package.json").toFile();
            try {
                prettyPrintingMapper.writeValue(packageJsonFile, packageJson);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    /**
     * Writes the Typescript file to the given directory.
     */
    private static void emit(TypescriptFile tsFile, File outputDir) {
        try {
            tsFile.writeTo(outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<String> externalImports(Collection<TypescriptFile> files) {
        Set<ImportStatement> imports = files.stream()
                .flatMap(file -> file.imports().stream())
                .collect(Collectors.toSet());
        return imports.stream()
                .map(ImportStatement::filepathToImport)
                // remove relative imports and @foundry/conjure-fe-lib which is a peer dependency
                .filter(path -> !path.startsWith("./") && !path.equals(CONJURE_FE_LIB))
                .collect(Collectors.toSet());
    }

}

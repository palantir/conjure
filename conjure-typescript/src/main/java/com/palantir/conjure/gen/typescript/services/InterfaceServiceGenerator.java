/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjurePackage;
import com.palantir.conjure.defs.types.TypeName;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterface;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class InterfaceServiceGenerator implements ServiceGenerator {

    @Override
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition, ConjureImports conjureImports) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types(), conjureImports,
                conjureDefinition.types().definitions().defaultConjurePackage());
        return conjureDefinition.services()
                .entrySet()
                .stream()
                .map(e -> generate(e.getKey(), e.getValue(), typeMapper))
                .collect(Collectors.toSet());
    }

    private TypescriptFile generate(TypeName typeName, ServiceDefinition serviceDef, TypeMapper typeMapper) {
        ConjurePackage packageLocation = serviceDef.conjurePackage();
        String parentFolderPath = GenerationUtils.packageToFolderPath(packageLocation);
        Set<TypescriptFunctionSignature> methodSignatures = serviceDef.endpoints().entrySet()
                .stream()
                .map(e -> ServiceUtils.generateFunctionSignature(e.getKey(), e.getValue(), typeMapper))
                .collect(Collectors.toSet());
        TypescriptInterface serviceInterface = TypescriptInterface.builder()
                .name(getInterfaceName(typeName))
                .methodSignatures(new TreeSet<>(methodSignatures))
                .build();
        return TypescriptFile.builder()
                .addEmittables(serviceInterface)
                .imports(ServiceUtils.generateImportStatements(serviceDef, typeName, packageLocation, typeMapper))
                .name(typeName.name())
                .parentFolderPath(parentFolderPath)
                .build();
    }

    @Override
    public Set<ExportStatement> generateExports(ConjureDefinition conjureDefinition) {
        return conjureDefinition.services()
                .entrySet()
                .stream()
                .map(e -> generateExport(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    private ExportStatement generateExport(TypeName typeName, ServiceDefinition serviceDef) {
        ConjurePackage packageLocation = serviceDef.conjurePackage();
        String parentFolderPath = GenerationUtils.packageToFolderPath(packageLocation);
        return GenerationUtils.createExportStatementRelativeToRoot(
                getInterfaceName(typeName), parentFolderPath, typeName.name());
    }

    private String getInterfaceName(TypeName typeName) {
        return "I" + typeName.name();
    }
}

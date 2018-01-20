/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.google.common.collect.Collections2;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterface;
import com.palantir.conjure.gen.typescript.poet.TypescriptInterfaceFunctionSignature;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class InterfaceServiceGenerator implements ServiceGenerator {

    @Override
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types());
        return conjureDefinition.services()
                .stream()
                .map(serviceDef -> generate(serviceDef, typeMapper))
                .collect(Collectors.toSet());
    }

    private TypescriptFile generate(ServiceDefinition serviceDef, TypeMapper typeMapper) {
        ConjurePackage packageLocation = serviceDef.serviceName().conjurePackage();
        String parentFolderPath = GenerationUtils.packageToScopeAndModule(packageLocation);
        Set<TypescriptInterfaceFunctionSignature> methodSignatures = serviceDef.endpoints().entrySet()
                .stream()
                .map(e -> TypescriptInterfaceFunctionSignature.builder()
                        .docs(e.getValue().docs())
                        .signature(ServiceUtils.generateFunctionSignature(e.getKey(), e.getValue(), typeMapper))
                        .build())
                .collect(Collectors.toSet());
        TypescriptInterface serviceInterface = TypescriptInterface.builder()
                .name(getInterfaceName(serviceDef.serviceName()))
                .methodSignatures(new TreeSet<>(methodSignatures))
                .build();
        return TypescriptFile.builder()
                .addEmittables(serviceInterface)
                .imports(ServiceUtils.generateImportStatements(serviceDef, typeMapper))
                .name(serviceDef.serviceName().name())
                .parentFolderPath(parentFolderPath)
                .build();
    }

    @Override
    public Map<ConjurePackage, Collection<ExportStatement>> generateExports(ConjureDefinition conjureDefinition) {
        Map<ConjurePackage, Set<ServiceDefinition>> definitionsByPackage =
                conjureDefinition.services().stream()
                        .collect(Collectors.groupingBy(def -> def.serviceName().conjurePackage(), Collectors.toSet()));
        return definitionsByPackage
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> generateExports(entry.getValue())));
    }

    private static Collection<ExportStatement> generateExports(Set<ServiceDefinition> definitions) {
        return Collections2.transform(definitions,
                typeAndDefinition -> generateExport(typeAndDefinition.serviceName()));
    }

    private static ExportStatement generateExport(TypeName typeName) {
        return GenerationUtils.createExportStatementRelativeToRoot(typeName.name(), getInterfaceName(typeName));
    }

    private static String getInterfaceName(TypeName typeName) {
        return "I" + typeName.name();
    }
}

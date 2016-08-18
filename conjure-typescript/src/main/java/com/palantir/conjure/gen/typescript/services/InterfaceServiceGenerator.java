/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
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
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types(),
                conjureDefinition.types().definitions().defaultPackage());
        return conjureDefinition.services()
                .entrySet()
                .stream()
                .map(e -> generate(e.getKey(), e.getValue(), typeMapper))
                .collect(Collectors.toSet());
    }

    private TypescriptFile generate(String clazz, ServiceDefinition serviceDef, TypeMapper typeMapper) {
        String packageLocation = serviceDef.packageName();
        String parentFolderPath = GenerationUtils.packageNameToFolderPath(packageLocation);
        Set<TypescriptFunctionSignature> methodSignatures = serviceDef.endpoints().entrySet()
                .stream()
                .map(e -> ServiceUtils.generateFunctionSignature(e.getKey(), e.getValue(), typeMapper))
                .collect(Collectors.toSet());
        TypescriptInterface serviceInterface = TypescriptInterface.builder()
                .name("I" + clazz)
                .methodSignatures(new TreeSet<>(methodSignatures))
                .build();
        return TypescriptFile.builder()
                .addEmittables(serviceInterface)
                .imports(ServiceUtils.generateImportStatements(serviceDef, clazz, packageLocation, typeMapper))
                .name(clazz)
                .parentFolderPath(parentFolderPath)
                .build();
    }
}

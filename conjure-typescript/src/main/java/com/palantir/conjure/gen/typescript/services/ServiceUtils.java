/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ServiceUtils {
    private ServiceUtils() {
        // No
    }

    public static List<ImportStatement> generateImportStatements(ServiceDefinition serviceDef,
            String name, String packageLocation, TypeMapper typeMapper) {
        List<ConjureType> usedTypes = serviceDef.endpoints().values().stream()
                .flatMap(endpoint -> {
                    Stream<ConjureType> endpointTypes = endpoint.argsWithAutoDefined().orElse(
                            Maps.newHashMap()).values().stream()
                            .map(ArgumentDefinition::type);
                    if (endpoint.returns().isPresent()) {
                        endpointTypes = Stream.concat(endpointTypes, Stream.of(endpoint.returns().get()));
                    }
                    return endpointTypes;
                })
                .collect(Collectors.toList());
        return GenerationUtils.generateImportStatements(usedTypes, name, packageLocation, typeMapper);
    }

    public static TypescriptFunctionSignature generateFunctionSignature(String name, EndpointDefinition value,
            TypeMapper typeMapper) {
        String innerReturnType = value.returns().map(
                type -> typeMapper.getTypescriptType(value.returns().get()).name()).orElse("void");
        return TypescriptFunctionSignature.builder()
                .name(name)
                .parameters(generateParameters(value.argsWithAutoDefined().orElse(ImmutableMap.of()), typeMapper))
                .returnType(TypescriptType.builder().name("Promise<" + innerReturnType + ">").build())
                .build();
    }

    private static List<TypescriptTypeSignature> generateParameters(Map<String, ArgumentDefinition> parameters,
            TypeMapper typeMapper) {
        return parameters.entrySet()
                .stream()
                .map(e -> (TypescriptTypeSignature) TypescriptTypeSignature.builder()
                        .isOptional(e.getValue().type() instanceof OptionalType)
                        .name(e.getKey())
                        .typescriptType(typeMapper.getTypescriptType(e.getValue().type()))
                        .build()
                )
                .collect(Collectors.toList());
    }
}

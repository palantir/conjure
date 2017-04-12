/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.ConjurePackage;
import com.palantir.conjure.gen.python.PackageNameProcessor;
import com.palantir.conjure.gen.python.poet.PythonClass;
import com.palantir.conjure.gen.python.poet.PythonClassName;
import com.palantir.conjure.gen.python.poet.PythonEndpointDefinition;
import com.palantir.conjure.gen.python.poet.PythonEndpointDefinition.PythonEndpointParam;
import com.palantir.conjure.gen.python.poet.PythonImport;
import com.palantir.conjure.gen.python.poet.PythonService;
import com.palantir.conjure.gen.python.types.DefaultTypeNameVisitor;
import com.palantir.conjure.gen.python.types.MyPyTypeNameVisitor;
import com.palantir.conjure.gen.python.types.ReferencedTypeNameVisitor;
import com.palantir.conjure.gen.python.types.TypeMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClientGenerator {

    public PythonClass generateClient(TypesDefinition types,
            ConjureImports importedTypes,
            PackageNameProcessor packageNameProvider,
            String serviceName, ServiceDefinition serviceDefinition) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(
                types, importedTypes, packageNameProvider);

        Builder<PythonClassName> referencedTypesBuilder = ImmutableSet.<PythonClassName>builder();

        List<PythonEndpointDefinition> endpoints = serviceDefinition.endpoints()
                .entrySet()
                .stream()
                .map(entry -> {
                    EndpointDefinition ed = entry.getValue();

                    ed.returns()
                            .ifPresent(returnType -> referencedTypesBuilder.addAll(returnType.visit(
                                    referencedTypeNameVisitor)));
                    ed.argsWithAutoDefined().ifPresent(args -> {
                        args.values().forEach(arg -> referencedTypesBuilder.addAll(arg.type().visit(
                                referencedTypeNameVisitor)));
                    });

                    List<PythonEndpointParam> params = ed.argsWithAutoDefined().map(args -> args.entrySet()
                            .stream()
                            .map(argEntry -> PythonEndpointParam
                                    .builder()
                                    .paramName(argEntry.getKey())
                                    .paramId(argEntry.getValue().paramId())
                                    .paramType(argEntry.getValue().paramType())
                                    .myPyType(myPyMapper.getTypeName(argEntry.getValue().type()))
                                    .build())
                            .collect(Collectors.toList()))
                            .orElse(ImmutableList.of());

                    return PythonEndpointDefinition.builder()
                            .methodName(entry.getKey())
                            .http(ed.http())
                            .authDefinition(ed.auth().orElse(serviceDefinition.defaultAuth()))
                            .params(params)
                            .pythonReturnType(ed.returns().map(mapper::getTypeName))
                            .myPyReturnType(ed.returns().map(myPyMapper::getTypeName))
                            .isBinary(ed.returns().map(rt -> rt instanceof BinaryType).orElse(false))
                            .build();
                })
                .collect(Collectors.toList());

        ConjurePackage packageName = packageNameProvider.getPackageName(
                Optional.of(serviceDefinition.conjurePackage()));

        List<PythonImport> imports = referencedTypesBuilder.build()
                .stream()
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(className -> PythonImport.of(className, Optional.empty()))
                .collect(Collectors.toList());

        return PythonService.builder()
                .packageName(packageName.name())
                .addAllRequiredImports(PythonService.DEFAULT_IMPORTS)
                .addAllRequiredImports(imports)
                .className(serviceName)
                .addAllEndpointDefinitions(endpoints)
                .build();

    }
}

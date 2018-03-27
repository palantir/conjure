/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.client;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.palantir.conjure.defs.visitor.TypeVisitor;
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
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.TypeDefinition;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClientGenerator {

    public PythonClass generateClient(
            List<TypeDefinition> types,
            PackageNameProcessor packageNameProvider,
            ServiceDefinition serviceDefinition) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(types, packageNameProvider);

        Builder<PythonClassName> referencedTypesBuilder = ImmutableSet.builder();

        List<PythonEndpointDefinition> endpoints = serviceDefinition.getEndpoints()
                .stream()
                .map(ed -> {
                    ed.getReturns()
                            .ifPresent(returnType -> referencedTypesBuilder.addAll(
                                    returnType.accept(referencedTypeNameVisitor)));
                    ed.getArgs().forEach(arg -> referencedTypesBuilder.addAll(
                            arg.getType().accept(referencedTypeNameVisitor)));

                    List<PythonEndpointParam> params = ed.getArgs()
                            .stream()
                            .map(argEntry -> PythonEndpointParam
                                    .builder()
                                    .paramName(argEntry.getArgName().get())
                                    .paramType(argEntry.getParamType())
                                    .myPyType(myPyMapper.getTypeName(argEntry.getType()))
                                    .build())
                            .collect(Collectors.toList());

                    return PythonEndpointDefinition.builder()
                            .methodName(ed.getEndpointName())
                            .httpMethod(ed.getHttpMethod())
                            .httpPath(ed.getHttpPath())
                            .auth(ed.getAuth())
                            .params(params)
                            .pythonReturnType(ed.getReturns().map(mapper::getTypeName))
                            .myPyReturnType(ed.getReturns().map(myPyMapper::getTypeName))
                            .isBinary(ed.getReturns().map(rt -> {
                                if (rt.accept(TypeVisitor.IS_PRIMITIVE)) {
                                    return rt.accept(TypeVisitor.PRIMITIVE).get() == PrimitiveType.Value.BINARY;
                                }
                                return false;
                            }).orElse(false))
                            .build();
                })
                .collect(Collectors.toList());

        String packageName =
                packageNameProvider.getPackageName(serviceDefinition.getServiceName().getPackage());
        List<PythonImport> imports = referencedTypesBuilder.build()
                .stream()
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(className -> PythonImport.of(className, Optional.empty()))
                .collect(Collectors.toList());

        return PythonService.builder()
                .packageName(packageName)
                .addAllRequiredImports(PythonService.DEFAULT_IMPORTS)
                .addAllRequiredImports(imports)
                .className(serviceDefinition.getServiceName().getName())
                .docs(serviceDefinition.getDocs())
                .addAllEndpointDefinitions(endpoints)
                .build();
    }
}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.client;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
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

    public PythonClass generateClient(
            List<TypeDefinition> types,
            PackageNameProcessor packageNameProvider,
            ServiceDefinition serviceDefinition) {

        TypeMapper mapper = new TypeMapper(new DefaultTypeNameVisitor(types));
        TypeMapper myPyMapper = new TypeMapper(new MyPyTypeNameVisitor(types));
        ReferencedTypeNameVisitor referencedTypeNameVisitor = new ReferencedTypeNameVisitor(types, packageNameProvider);

        Builder<PythonClassName> referencedTypesBuilder = ImmutableSet.builder();

        List<PythonEndpointDefinition> endpoints = serviceDefinition.endpoints()
                .stream()
                .map(ed -> {
                    ed.returns()
                            .ifPresent(returnType -> referencedTypesBuilder.addAll(returnType.visit(
                                    referencedTypeNameVisitor)));
                    ed.args().forEach(arg -> referencedTypesBuilder.addAll(arg.type().visit(
                            referencedTypeNameVisitor)));

                    List<PythonEndpointParam> params = ed.args()
                            .stream()
                            .map(argEntry -> PythonEndpointParam
                                    .builder()
                                    .paramName(argEntry.argName().name())
                                    .paramId(argEntry.paramId())
                                    .paramType(argEntry.paramType())
                                    .myPyType(myPyMapper.getTypeName(argEntry.type()))
                                    .build())
                            .collect(Collectors.toList());

                    return PythonEndpointDefinition.builder()
                            .methodName(ed.endpointName())
                            .http(ed.http())
                            .auth(ed.auth())
                            .params(params)
                            .pythonReturnType(ed.returns().map(mapper::getTypeName))
                            .myPyReturnType(ed.returns().map(myPyMapper::getTypeName))
                            .isBinary(ed.returns().map(rt -> rt instanceof BinaryType).orElse(false))
                            .build();
                })
                .collect(Collectors.toList());

        ConjurePackage packageName =
                packageNameProvider.getPackageName(serviceDefinition.serviceName().conjurePackage());
        List<PythonImport> imports = referencedTypesBuilder.build()
                .stream()
                .filter(entry -> !entry.conjurePackage().equals(packageName)) // don't need to import if in this file
                .map(className -> PythonImport.of(className, Optional.empty()))
                .collect(Collectors.toList());

        return PythonService.builder()
                .packageName(packageName.name())
                .addAllRequiredImports(PythonService.DEFAULT_IMPORTS)
                .addAllRequiredImports(imports)
                .className(serviceDefinition.serviceName().name())
                .docs(serviceDefinition.docs())
                .addAllEndpointDefinitions(endpoints)
                .build();
    }
}

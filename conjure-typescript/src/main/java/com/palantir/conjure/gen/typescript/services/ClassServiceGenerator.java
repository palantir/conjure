/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.gen.typescript.poet.ArrayExpression;
import com.palantir.conjure.gen.typescript.poet.AssignStatement;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.FunctionCallExpression;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.JsonExpression;
import com.palantir.conjure.gen.typescript.poet.RawExpression;
import com.palantir.conjure.gen.typescript.poet.ReturnStatement;
import com.palantir.conjure.gen.typescript.poet.StringExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptClass;
import com.palantir.conjure.gen.typescript.poet.TypescriptConstructor;
import com.palantir.conjure.gen.typescript.poet.TypescriptExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunction;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionBody;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClassServiceGenerator implements ServiceGenerator {

    @Override
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition, ConjureImports conjureImports) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types(), conjureImports,
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
        TypescriptFunctionBody constructorBody = TypescriptFunctionBody.builder().addStatements(
                AssignStatement.builder()
                        .lhs("this.bridge")
                        .rhs(RawExpression.of("bridge"))
                        .build())
                .build();
        TypescriptType bridgeType = TypescriptType.builder().name("IHttpApiBridge").build();
        TypescriptConstructor constructor = TypescriptConstructor.builder()
                .addParameters(TypescriptTypeSignature.builder().name("bridge").typescriptType(bridgeType).build())
                .functionBody(constructorBody).build();
        Set<TypescriptFunction> methods = serviceDef.endpoints().entrySet()
                .stream()
                .map(e -> {
                    TypescriptFunctionSignature functionSignature = ServiceUtils.generateFunctionSignature(e.getKey(),
                            e.getValue(), typeMapper);
                    TypescriptFunctionBody functionBody = generateFunctionBody(serviceDef.basePath(), e.getKey(),
                            e.getValue(), serviceDef.defaultAuth(), typeMapper);
                    return TypescriptFunction.builder().functionSignature(functionSignature)
                            .functionBody(functionBody).build();
                })
                .collect(Collectors.toSet());
        List<AssignStatement> fields = Lists.newArrayList(
                AssignStatement.builder().lhs("private bridge: IHttpApiBridge").build());
        TypescriptClass typescriptClass = TypescriptClass.builder()
                .constructor(Optional.of(constructor))
                .fields(fields)
                .name(clazz)
                .methods(methods)
                .build();
        return TypescriptFile.builder()
                .addEmittables(typescriptClass)
                .imports(ServiceUtils.generateImportStatements(serviceDef, clazz, packageLocation, typeMapper))
                .addImports(ImportStatement.builder()
                        .addNamesToImport("IHttpApiBridge")
                        .filepathToImport("@elements/conjure-fe-lib")
                        .build())
                .name(getFilename(clazz))
                .parentFolderPath(parentFolderPath)
                .build();
    }

    private TypescriptFunctionBody generateFunctionBody(String serviceBasePath, String name, EndpointDefinition value,
            AuthDefinition defaultAuth, TypeMapper typeMapper) {
        AuthDefinition authDefinition = value.auth().orElse(defaultAuth);

        Map<String, TypescriptExpression> keyValues = ImmutableMap.<String, TypescriptExpression>builder()
                .put("endpointPath", StringExpression.of(serviceBasePath + value.http().path()))
                .put("endpointName", StringExpression.of(name))
                .put("method", StringExpression.of(value.http().method()))
                // TODO(rmcnamara): support other request types
                .put("requestMediaType", StringExpression.of("application/json"))
                // TODO(rmcnamara): support other request types
                .put("responseMediaType", StringExpression.of("application/json"))
                .put("requiredHeaders", ArrayExpression.of(
                        authDefinition.type() == AuthDefinition.AuthType.HEADER
                                ? Lists.newArrayList(StringExpression.of("Authorization"))
                                : Lists.newArrayList()))
                .put("pathArguments", ArrayExpression.of(
                        value.argsWithAutoDefined().orElse(Maps.newHashMap()).entrySet().stream()
                                .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.PATH)
                                .map(e -> e.getKey())
                                .map(RawExpression::of)
                                .collect(Collectors.toList())))
                .put("queryArguments", JsonExpression.builder().keyValues(
                        value.argsWithAutoDefined().orElse(Maps.newHashMap()).entrySet().stream()
                                .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.QUERY)
                                .map(e -> e.getKey())
                                .collect(Collectors.toMap(identifier -> identifier,
                                        identifier -> RawExpression.of(identifier))))
                        .build())
                .put("data", Iterables.getOnlyElement(
                        value.argsWithAutoDefined().orElse(Maps.newHashMap()).entrySet().stream()
                                .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.BODY)
                                .map(e -> e.getKey())
                                .map(RawExpression::of)
                                .collect(Collectors.toList()), RawExpression.of("undefined")))
                .build();
        String genericParam = value.returns().map(val -> typeMapper.getTypescriptType(val).name()).orElse("void");
        FunctionCallExpression call = FunctionCallExpression.builder().name(
                "this.bridge.callEndpoint<" + genericParam + ">").addArguments(
                JsonExpression.builder().keyValues(keyValues).build()).build();
        return TypescriptFunctionBody.builder().addStatements(
                ReturnStatement.builder().expression(call).build()).build();
    }

    @Override
    public Set<ExportStatement> generateExports(ConjureDefinition conjureDefinition) {
        return conjureDefinition.services()
                .entrySet()
                .stream()
                .map(e -> generateExport(e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
    }

    private ExportStatement generateExport(String clazz, ServiceDefinition serviceDef) {
        String packageLocation = serviceDef.packageName();
        String parentFolderPath = GenerationUtils.packageNameToFolderPath(packageLocation);
        return GenerationUtils.createExportStatementRelativeToRoot(clazz, parentFolderPath, getFilename(clazz));
    }

    private String getFilename(String clazz) {
        return clazz + "Impl";
    }
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ParameterName;
import com.palantir.conjure.defs.services.PathDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.gen.typescript.ConjureTypeScriptClientGenerator;
import com.palantir.conjure.gen.typescript.poet.ArrayExpression;
import com.palantir.conjure.gen.typescript.poet.AssignStatement;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.FunctionCallExpression;
import com.palantir.conjure.gen.typescript.poet.JsonExpression;
import com.palantir.conjure.gen.typescript.poet.ObjectExpression;
import com.palantir.conjure.gen.typescript.poet.RawExpression;
import com.palantir.conjure.gen.typescript.poet.ReturnStatement;
import com.palantir.conjure.gen.typescript.poet.StandardImportStatement;
import com.palantir.conjure.gen.typescript.poet.StringExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptClass;
import com.palantir.conjure.gen.typescript.poet.TypescriptConstructor;
import com.palantir.conjure.gen.typescript.poet.TypescriptExpression;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunction;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionBody;
import com.palantir.conjure.gen.typescript.poet.TypescriptFunctionSignature;
import com.palantir.conjure.gen.typescript.poet.TypescriptSimpleType;
import com.palantir.conjure.gen.typescript.poet.TypescriptTypeSignature;
import com.palantir.conjure.gen.typescript.types.TypeMapper;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;

public final class ClassServiceGenerator implements ServiceGenerator {

    @Override
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types(),
                conjureDefinition.types().definitions().defaultConjurePackage());
        return conjureDefinition.services()
                .entrySet()
                .stream()
                .map(e -> generate(e.getKey(), e.getValue(), typeMapper))
                .collect(Collectors.toSet());
    }

    private TypescriptFile generate(TypeName typeName, ServiceDefinition serviceDef, TypeMapper typeMapper) {
        ConjurePackage packageLocation = serviceDef.conjurePackage();
        String parentFolderPath = GenerationUtils.packageToScopeAndModule(packageLocation);
        TypescriptFunctionBody constructorBody = TypescriptFunctionBody.builder().addStatements(
                AssignStatement.builder()
                        .lhs("this.bridge")
                        .rhs(RawExpression.of("bridge"))
                        .build())
                .build();
        TypescriptSimpleType bridgeType = TypescriptSimpleType.of("IHttpApiBridge");
        TypescriptConstructor constructor = TypescriptConstructor.builder()
                .addParameters(TypescriptTypeSignature.builder().name("bridge").typescriptType(bridgeType).build())
                .functionBody(constructorBody).build();
        List<TypescriptFunction> methods = serviceDef.endpoints().entrySet()
                .stream()
                .map(e -> {
                    TypescriptFunctionSignature functionSignature = ServiceUtils.generateFunctionSignature(e.getKey(),
                            e.getValue(), typeMapper);
                    TypescriptFunctionBody functionBody = generateFunctionBody(serviceDef.basePath(), e.getKey(),
                            e.getValue(), serviceDef.defaultAuth(), typeMapper);
                    return TypescriptFunction.builder().functionSignature(functionSignature)
                            .functionBody(functionBody).build();
                })
                .sorted(Comparator.comparing(TypescriptFunction::functionSignature))
                .collect(Collectors.toList());
        List<AssignStatement> fields = Lists.newArrayList(
                AssignStatement.builder().lhs("private bridge: IHttpApiBridge").build());
        TypescriptClass typescriptClass = TypescriptClass.builder()
                .constructor(Optional.of(constructor))
                .fields(fields)
                .name(typeName.name())
                .methods(methods)
                .build();
        return TypescriptFile.builder()
                .addEmittables(typescriptClass)
                .imports(ServiceUtils.generateImportStatements(serviceDef, typeName, packageLocation, typeMapper))
                .addImports(StandardImportStatement.builder()
                        .addNamesToImport("IHttpApiBridge")
                        .filepathToImport(ConjureTypeScriptClientGenerator.CONJURE_FE_LIB)
                        .build())
                .name(getFilename(typeName))
                .parentFolderPath(parentFolderPath)
                .build();
    }

    private TypescriptFunctionBody generateFunctionBody(PathDefinition serviceBasePath, String name,
            EndpointDefinition value, AuthDefinition defaultAuth, TypeMapper typeMapper) {
        AuthDefinition authDefinition = value.auth().orElse(defaultAuth);

        String responseMediaType;
        if (value.returns().map(type -> type instanceof BinaryType).orElse(false)) {
            responseMediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            responseMediaType = MediaType.APPLICATION_JSON;
        }

        boolean consumesTypeIsBinary = value.args().map(
                args -> args.values().stream()
                        .anyMatch(arg -> arg.type() instanceof BinaryType && arg.paramType().equals(
                                ArgumentDefinition.ParamType.BODY))).orElse(false);

        String requestMediaType;
        if (consumesTypeIsBinary) {
            requestMediaType = MediaType.APPLICATION_OCTET_STREAM;
        } else {
            requestMediaType = MediaType.APPLICATION_JSON;
        }

        Map<ParameterName, ArgumentDefinition> args = value.argsWithAutoDefined().orElse(Collections.emptyMap());

        ObjectExpression headers = ObjectExpression.builder().keyValues(
                args.entrySet().stream()
                        .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.HEADER)
                        .collect(Collectors.toMap(
                                e -> StringExpression.of(e.getValue().paramId().orElse(e.getKey()).name()),
                                e -> RawExpression.of(e.getKey().name()))))
                .build();

        ArrayExpression requiredHeaders = ArrayExpression.of(ImmutableList.<TypescriptExpression>builder()
                .addAll(authDefinition.type() == AuthDefinition.AuthType.HEADER
                        ? Lists.newArrayList(StringExpression.of("Authorization"))
                        : Lists.newArrayList())
                .addAll(args.entrySet().stream()
                        .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.HEADER)
                        .map(e -> StringExpression.of(e.getValue().paramId().orElse(e.getKey()).name()))
                        .collect(Collectors.toList()))
                .build());

        ArrayExpression pathArguments = ArrayExpression.of(
                args.entrySet().stream()
                        .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.PATH)
                        .map(e -> e.getKey().name())
                        .map(RawExpression::of)
                        .collect(Collectors.toList()));

        JsonExpression queryArguments = JsonExpression.builder()
                .keyValues(args.entrySet().stream()
                        .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.QUERY)
                        .collect(Collectors.toMap(
                                arg -> {
                                    ParameterName parameterName = arg.getValue().paramId().orElse(arg.getKey());
                                    return parameterName.name();
                                },
                                arg -> RawExpression.of(arg.getKey().name()))))
                .build();

        RawExpression data = Iterables.getOnlyElement(
                args.entrySet().stream()
                        .filter(e -> e.getValue().paramType() == ArgumentDefinition.ParamType.BODY)
                        .map(e -> e.getKey().name())
                        .map(RawExpression::of)
                        .collect(Collectors.toList()), RawExpression.of("undefined"));

        Map<String, TypescriptExpression> keyValues = ImmutableMap.<String, TypescriptExpression>builder()
                .put("endpointPath", StringExpression.of(
                        serviceBasePath.resolve(value.http().path()).path().toString()))
                .put("endpointName", StringExpression.of(name))
                .put("headers", headers)
                .put("method", StringExpression.of(value.http().method()))
                .put("requestMediaType", StringExpression.of(requestMediaType))
                .put("responseMediaType", StringExpression.of(responseMediaType))
                .put("requiredHeaders", requiredHeaders)
                .put("pathArguments", pathArguments)
                .put("queryArguments", queryArguments)
                .put("data", data)
                .build();
        String genericParam = ServiceUtils.generateFunctionSignatureReturnType(value, typeMapper);
        FunctionCallExpression call = FunctionCallExpression.builder().name(
                "this.bridge.callEndpoint<" + genericParam + ">").addArguments(
                JsonExpression.builder().keyValues(keyValues).build()).build();
        return TypescriptFunctionBody.builder().addStatements(
                ReturnStatement.of(call)).build();
    }

    @Override
    public Map<ConjurePackage, Collection<ExportStatement>> generateExports(ConjureDefinition conjureDefinition) {
        Map<ConjurePackage, Set<Map.Entry<TypeName, ServiceDefinition>>> definitionsByPackage =
                conjureDefinition.services().entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getValue().conjurePackage(), Collectors.toSet()));
        return definitionsByPackage
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> generateExports(entry.getValue())));
    }

    private static Collection<ExportStatement> generateExports(
            Set<Map.Entry<TypeName, ServiceDefinition>> definitions) {
        return Collections2.transform(definitions, typeAndDefinition -> generateExport(typeAndDefinition.getKey()));
    }

    private static ExportStatement generateExport(TypeName typeName) {
        return GenerationUtils.createExportStatementRelativeToRoot(typeName.name(), getFilename(typeName));
    }

    private static String getFilename(TypeName typeName) {
        return typeName.name() + "Impl";
    }
}

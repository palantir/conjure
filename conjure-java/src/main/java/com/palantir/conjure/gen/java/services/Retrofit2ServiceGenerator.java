/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.types.Retrofit2ReturnTypeClassNameVisitor;
import com.palantir.conjure.gen.java.types.TypeMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Retrofit2ServiceGenerator implements ServiceGenerator {

    private static final ClassName CALL_TYPE = ClassName.get("retrofit2", "Call");

    private static final Logger log = LoggerFactory.getLogger(Retrofit2ServiceGenerator.class);

    @Override
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types(), imports);
        TypeMapper returnTypeMapper =
                new TypeMapper(conjureDefinition.types(), imports, Retrofit2ReturnTypeClassNameVisitor::new);
        return conjureDefinition.services().entrySet().stream()
                .map(entry -> generateService(entry.getKey(), entry.getValue(), typeMapper, returnTypeMapper))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(String serviceName, ServiceDefinition serviceDefinition,
            TypeMapper typeMapper, TypeMapper returnTypeMapper) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(Retrofit2ServiceGenerator.class));

        if (serviceDefinition.docs().isPresent()) {
            serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(serviceDefinition.docs().get(), "\n"));
        }

        serviceBuilder.addMethods(serviceDefinition.endpoints().entrySet().stream()
                .map(endpoint -> generateServiceMethod(
                        endpoint.getKey(),
                        endpoint.getValue(),
                        serviceDefinition.basePath(),
                        serviceDefinition.defaultAuth(),
                        typeMapper,
                        returnTypeMapper))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDefinition.packageName(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec generateServiceMethod(
            String endpointName,
            EndpointDefinition endpointDef,
            String basePath,
            AuthDefinition defaultAuth,
            TypeMapper typeMapper,
            TypeMapper returnTypeMapper) {
        Set<String> encodedPathArgs = extractEncodedPathArgs(endpointDef.http().path());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(AnnotationSpec.builder(httpMethodToClassName(endpointDef.http().method()))
                        .addMember("value", "$S", constructPath(basePath, endpointDef.http().path(), encodedPathArgs))
                        .build());

        if (endpointDef.returns().map(type -> type instanceof BinaryType).orElse(false)) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Streaming")).build());
        }

        endpointDef.deprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(
                ClassName.get("java.lang", "Deprecated")));

        ServiceGenerator.getJavaDoc(endpointDef).ifPresent(
                content -> methodBuilder.addJavadoc("$L", content.toString()));

        if (endpointDef.returns().isPresent()) {
            methodBuilder.returns(
                    ParameterizedTypeName.get(CALL_TYPE,
                            returnTypeMapper.getClassName(endpointDef.returns().get()).box()));
        } else {
            methodBuilder.returns(ParameterizedTypeName.get(CALL_TYPE, ClassName.get(Void.class)));
        }

        Set<String> pathArgs = endpointDef.http().pathArgs();

        getAuthParameter(methodBuilder, endpointDef.auth().orElse(defaultAuth)).ifPresent(methodBuilder::addParameter);

        endpointDef.argsWithAutoDefined().ifPresent(args -> methodBuilder.addParameters(args.entrySet().stream()
                .map(arg -> createEndpointParameter(
                        typeMapper, encodedPathArgs, arg.getKey(), arg.getValue()))
                .collect(Collectors.toList())));

        return methodBuilder.build();
    }

    private Set<String> extractEncodedPathArgs(String path) {
        Pattern pathArg = Pattern.compile("\\{([^\\}]+)\\}");
        Matcher matcher = pathArg.matcher(path);
        ImmutableSet.Builder<String> encodedArgs = ImmutableSet.builder();
        while (matcher.find()) {
            String arg = matcher.group(1);
            if (arg.contains(":")) {
                // Strip everything after first colon
                encodedArgs.add(arg.substring(0, arg.indexOf(':')));
            }
        }
        return encodedArgs.build();
    }

    private String constructPath(String basePath, String endpointPath, Set<String> encodedPathArgs) {
        // For encoded arguments, strip everything after argument name in endpointPath
        String purifiedEndpointPath = replaceEncodedPathArgs(endpointPath, 0, Lists.newArrayList(encodedPathArgs));
        return basePath + StringUtils.prependIfMissing(purifiedEndpointPath, "/");
    }

    private String replaceEncodedPathArgs(String path, int currentArg, List<String> encodedPathArgs) {
        if (currentArg >= encodedPathArgs.size()) {
            return path;
        }
        String pattern = String.format("\\{%s([^\\}]+)\\}", encodedPathArgs.get(currentArg));
        String replacement = String.format("{%s}", encodedPathArgs.get(currentArg));
        return replaceEncodedPathArgs(path.replaceFirst(pattern, replacement), currentArg + 1, encodedPathArgs);
    }

    private ParameterSpec createEndpointParameter(TypeMapper typeMapper, Set<String> encodedPathArgs, String paramKey,
            ArgumentDefinition def) {
        ParameterSpec.Builder param = ParameterSpec.builder(
                typeMapper.getClassName(def.type()),
                paramKey);

        switch (def.paramType()) {
            case PATH:
                String pathParamKey = def.paramId().orElse(paramKey);
                AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("retrofit2.http", "Path"))
                        .addMember("value", "$S", pathParamKey);
                if (encodedPathArgs.contains(pathParamKey)) {
                    builder.addMember("encoded", "$L", true);
                }
                param.addAnnotation(builder.build());
                break;
            case QUERY:
                param.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Query"))
                        .addMember("value", "$S", def.paramId().orElse(paramKey))
                        .build());
                break;
            case HEADER:
                param.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Header"))
                        .addMember("value", "$S", def.paramId().orElse(paramKey))
                        .build());
                break;
            case BODY:
                param.addAnnotation(ClassName.get("retrofit2.http", "Body"));
                break;
            default:
                throw new IllegalStateException("Unrecognized argument type: " + def.paramType());
        }
        return param.build();
    }

    private Optional<ParameterSpec> getAuthParameter(MethodSpec.Builder methodBuilder, AuthDefinition auth) {
        switch (auth.type()) {
            case HEADER:
                return Optional.of(
                        ParameterSpec.builder(ClassName.get("com.palantir.tokens.auth", "AuthHeader"), "authHeader")
                                .addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Header"))
                                        .addMember("value", "$S", auth.id())
                                        .build())
                                .build());
            case COOKIE:
                // TODO(melliot): generate required retrofit logic to support this
                log.error("Retrofit does not support Cookie arguments");
                break;
            case NONE:
                /* do nothing */
                break;
            default:
                throw new IllegalArgumentException("Unknown authorization type: " + auth.type());
        }
        return Optional.empty();
    }

    private static ClassName httpMethodToClassName(String method) {
        switch (method) {
            case "DELETE":
                return ClassName.get("retrofit2.http", "DELETE");
            case "GET":
                return ClassName.get("retrofit2.http", "GET");
            case "PUT":
                return ClassName.get("retrofit2.http", "PUT");
            case "POST":
                return ClassName.get("retrofit2.http", "POST");
            default:
                throw new IllegalArgumentException("Unrecognized HTTP method: " + method);
        }
    }

}

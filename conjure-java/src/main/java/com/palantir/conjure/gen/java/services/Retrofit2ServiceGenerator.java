/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.types.TypeMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Retrofit2ServiceGenerator implements ServiceGenerator {

    private static final ClassName CALL_TYPE = ClassName.get("retrofit2.http", "Call");

    private static final Logger log = LoggerFactory.getLogger(Retrofit2ServiceGenerator.class);

    private final Settings settings;

    public Retrofit2ServiceGenerator(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper typeMapper = new TypeMapper(conjureDefinition.types(), settings.optionalTypeStrategy());
        return conjureDefinition.services().entrySet().stream()
                .map(entry -> generateService(entry.getKey(), entry.getValue(), typeMapper))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(String serviceName, ServiceDefinition serviceDefinition, TypeMapper typeMapper) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC);

        if (serviceDefinition.docs().isPresent()) {
            serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(serviceDefinition.docs().get(), "\n"));
        }

        serviceBuilder.addMethods(serviceDefinition.endpoints().entrySet().stream()
                .map(endpoint -> generateServiceMethod(
                        endpoint.getKey(),
                        endpoint.getValue(),
                        serviceDefinition.defaultAuth(), typeMapper))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDefinition.packageName(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec generateServiceMethod(
            String endpointName,
            EndpointDefinition endpointDef,
            AuthDefinition defaultAuth, TypeMapper typeMapper) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(AnnotationSpec.builder(httpMethodToClassName(endpointDef.http().method()))
                        .addMember("value", "$S", endpointDef.http().path())
                        .build());

        endpointDef.docs().ifPresent(docs -> methodBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

        if (endpointDef.returns().isPresent()) {
            methodBuilder.returns(
                    ParameterizedTypeName.get(CALL_TYPE, typeMapper.getClassName(endpointDef.returns().get())));
        } else {
            methodBuilder.returns(ParameterizedTypeName.get(CALL_TYPE, ClassName.get(Void.class)));
        }

        Set<String> pathArgs = endpointDef.http().pathArgs();

        getAuthParameter(methodBuilder, endpointDef.auth().orElse(defaultAuth)).ifPresent(methodBuilder::addParameter);

        endpointDef.args().ifPresent(args -> methodBuilder.addParameters(args.entrySet().stream()
                .map(arg -> createEndpointParameter(typeMapper, pathArgs, arg.getKey(), arg.getValue()))
                .collect(Collectors.toList())));

        return methodBuilder.build();
    }

    private ParameterSpec createEndpointParameter(TypeMapper typeMapper, Set<String> pathArgs,
            String paramKey, ArgumentDefinition def) {
        ParameterSpec.Builder param = ParameterSpec.builder(
                typeMapper.getClassName(def.type()),
                paramKey);

        switch (def.paramType()) {
            case AUTO:
            case PATH:
                if (pathArgs.contains(paramKey)) {
                    param.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Path"))
                            .addMember("value", "$S", def.paramId().orElse(paramKey))
                            .build());
                } else {
                    param.addAnnotation(ClassName.get("retrofit2.http", "Body"));
                }
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
                        ParameterSpec.builder(ClassName.get("com.palantir.tokens", "AuthHeader"), "authHeader")
                                .addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Header"))
                                        .addMember("value", "$S", auth.id())
                                        .build())
                                .build());
            case COOKIE:
                // TODO generate required retrofit logic to support this
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

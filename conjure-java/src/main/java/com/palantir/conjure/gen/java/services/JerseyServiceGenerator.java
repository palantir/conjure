/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.google.common.base.Throwables;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.StringCleanup;
import com.palantir.conjure.gen.java.TypeMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public final class JerseyServiceGenerator {

    private TypeMapper typeMapper;
    private Map<String, ServiceDefinition> services;

    public JerseyServiceGenerator(ConjureDefinition def, Settings settings) {
        this.services = def.services();
        this.typeMapper = new TypeMapper(def.types(), settings.optionalTypeStrategy());
    }

    public Set<JavaFile> generate() {
        return services.entrySet().stream()
                .map(entry -> generateService(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public void emit(File outputDir) {
        services.entrySet().stream()
                .map(entry -> generateService(entry.getKey(), entry.getValue()))
                .forEach(f -> {
                    try {
                        f.writeTo(outputDir);
                    } catch (IOException e) {
                        Throwables.propagate(e);
                    }
                });
    }

    private JavaFile generateService(String serviceName, ServiceDefinition serviceDef) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", serviceDef.basePath())
                        .build());

        serviceDef.docs().ifPresent(docs -> serviceBuilder.addJavadoc("$L", StringCleanup.withEndOfLine(docs)));

        serviceBuilder.addMethods(serviceDef.endpoints().entrySet().stream()
                .map(endpoint -> generateServiceMethod(
                        endpoint.getKey(),
                        endpoint.getValue(),
                        serviceDef.defaultAuth()))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDef.packageName(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec generateServiceMethod(
            String endpointName,
            EndpointDefinition endpointDef,
            AuthDefinition defaultAuth) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(httpMethodToClassName(endpointDef.http().method()))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", endpointDef.http().path())
                        .build());

        endpointDef.docs().ifPresent(docs -> methodBuilder.addJavadoc("$L", StringCleanup.withEndOfLine(docs)));

        endpointDef.returns().ifPresent(type -> methodBuilder.returns(typeMapper.getClassName(type)));

        Set<String> pathArgs = endpointDef.http().pathArgs();

        AuthDefinition auth = endpointDef.auth().orElse(defaultAuth);
        switch (auth.type()) {
            case HEADER:
                methodBuilder.addParameter(
                        ParameterSpec.builder(ClassName.get("com.palantir.tokens", "AuthHeader"), "authHeader")
                                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "HeaderParam"))
                                        .addMember("value", "$S", auth.id())
                                        .build())
                                .build());
                break;
            case COOKIE:
                methodBuilder.addParameter(
                        ParameterSpec.builder(ClassName.get("com.palantir.tokens", "AuthHeader"), "authHeader")
                                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "CookieParam"))
                                        .addMember("value", "$S", auth.id())
                                        .build())
                                .build());
                break;
            case NONE:
                /* do nothing */
                break;
            default:
                throw new IllegalArgumentException("Unknown authorization type: " + auth.type());
        }

        endpointDef.args().ifPresent(args -> methodBuilder.addParameters(args.entrySet().stream()
                .map(arg -> {
                    ArgumentDefinition def = arg.getValue();
                    ParameterSpec.Builder param = ParameterSpec.builder(
                            typeMapper.getClassName(def.type()),
                            arg.getKey());

                    switch (def.paramType()) {
                        case AUTO:
                        case PATH:
                            if (pathArgs.contains(arg.getKey())) {
                                param.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "PathParam"))
                                        .addMember("value", "$S", def.paramId().orElse(arg.getKey()))
                                        .build());
                            }
                            break;
                        case QUERY:
                            param.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "QueryParam"))
                                    .addMember("value", "$S", def.paramId().orElse(arg.getKey()))
                                    .build());
                            break;
                        case HEADER:
                            param.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "HeaderParam"))
                                    .addMember("value", "$S", def.paramId().orElse(arg.getKey()))
                                    .build());
                            break;
                        case BODY:
                            /* no annotations for body parameters */
                            break;
                        default:
                            throw new IllegalStateException("Unrecognized argument type: " + def.paramType());
                    }
                    return param.build();
                })
                .collect(Collectors.toList())));

        return methodBuilder.build();
    }

    private static ClassName httpMethodToClassName(String method) {
        switch (method) {
            case "DELETE":
                return ClassName.get("javax.ws.rs", "DELETE");
            case "GET":
                return ClassName.get("javax.ws.rs", "GET");
            case "PUT":
                return ClassName.get("javax.ws.rs", "PUT");
            case "POST":
                return ClassName.get("javax.ws.rs", "POST");
            default:
                throw new IllegalArgumentException("Unrecognized HTTP method: " + method);
        }
    }

}

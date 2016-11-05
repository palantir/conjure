/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.types.TypeMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class JerseyServiceGenerator implements ServiceGenerator {

    private final Settings settings;

    public JerseyServiceGenerator(Settings settings) {
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
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", serviceDefinition.basePath())
                        .build())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Consumes"))
                        .addMember("value", "$T.APPLICATION_JSON", ClassName.get("javax.ws.rs.core", "MediaType"))
                        .build())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Produces"))
                        .addMember("value", "$T.APPLICATION_JSON", ClassName.get("javax.ws.rs.core", "MediaType"))
                        .build())
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(JerseyServiceGenerator.class));

        serviceDefinition.docs().ifPresent(docs ->
                serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

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
            AuthDefinition defaultAuth,
            TypeMapper typeMapper) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(httpMethodToClassName(endpointDef.http().method()))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", endpointDef.http().path())
                        .build());
        endpointDef.deprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(
                ClassName.get("java.lang", "Deprecated")));

        ServiceGenerator.getJavaDoc(endpointDef).ifPresent(
                content -> methodBuilder.addJavadoc("$L", content.toString()));

        endpointDef.returns().ifPresent(type -> methodBuilder.returns(typeMapper.getClassName(type)));

        methodBuilder.addParameters(createParameterSpecs(endpointDef, defaultAuth, typeMapper));

        return methodBuilder.build();
    }

    private static List<ParameterSpec> createParameterSpecs(
            EndpointDefinition endpointDef,
            AuthDefinition defaultAuth,
            TypeMapper typeMapper) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        AuthDefinition auth = endpointDef.auth().orElse(defaultAuth);

        Optional<ParameterSpec> authParameterSpec = createAuthParameterSpec(auth);

        Set<String> pathArgs = endpointDef.http().pathArgs();
        Optional<List<ParameterSpec>> otherParameterSpecs = endpointDef.args().map(
                args -> createOtherParameterSpecs(typeMapper, pathArgs, args));

        authParameterSpec.ifPresent(parameterSpecs::add);
        otherParameterSpecs.ifPresent(parameterSpecs::addAll);
        return ImmutableList.copyOf(parameterSpecs);
    }

    private static Optional<ParameterSpec> createAuthParameterSpec(AuthDefinition auth) {
        Optional<String> annotationName = authTypeToAnnotationName(auth.type());
        return annotationName.map(name ->
                ParameterSpec.builder(ClassName.get("com.palantir.tokens.auth", "AuthHeader"), "authHeader")
                        .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", name))
                                .addMember("value", "$S", auth.id())
                                .build())
                        .build());
    }

    private static Optional<String> authTypeToAnnotationName(AuthDefinition.AuthType authType) {
        switch (authType) {
            case HEADER:
                return Optional.of("HeaderParam");
            case COOKIE:
                return Optional.of("CookieParam");
            case NONE:
                return Optional.empty();
            default:
                throw new IllegalArgumentException("Unknown authorization type: " + authType);
        }
    }

    private static List<ParameterSpec> createOtherParameterSpecs(
            TypeMapper typeMapper,
            Set<String> pathArgs,
            Map<String, ArgumentDefinition> args) {
        return args.entrySet().stream().map(arg -> {
            String argName = arg.getKey();
            ArgumentDefinition def = arg.getValue();
            ParameterSpec.Builder param = ParameterSpec.builder(
                    typeMapper.getClassName(def.type()),
                    arg.getKey());
            createParamTypeAnnotation(argName, def, pathArgs).ifPresent(param::addAnnotation);
            List<AnnotationSpec> markers = createMarkers(typeMapper, def.markers());
            markers.forEach(param::addAnnotation);
            return param.build();
        }).collect(Collectors.toList());
    }

    private static Optional<AnnotationSpec> createParamTypeAnnotation(
            String argName,
            ArgumentDefinition def,
            Set<String> pathArgs) {
        final String annotationType;
        switch (def.paramType()) {
            case AUTO:
            case PATH:
                if (!pathArgs.contains(argName)) {
                    return Optional.empty();
                }
                annotationType = "PathParam";
                break;
            case QUERY:
                annotationType = "QueryParam";
                break;
            case HEADER:
                annotationType = "HeaderParam";
                break;
            case BODY:
                /* no annotations for body parameters */
                return Optional.empty();
            default:
                throw new IllegalStateException("Unrecognized argument type: " + def.paramType());
        }
        return Optional.of(AnnotationSpec.builder(ClassName.get("javax.ws.rs", annotationType))
                .addMember("value", "$S", def.paramId().orElse(argName))
                .build());
    }

    private static List<AnnotationSpec> createMarkers(TypeMapper typeMapper, List<ConjureType> markers) {
        List<ConjureType> nonReferenceTypes = markers.stream()
                .filter(type -> !(type instanceof ReferenceType))
                .collect(Collectors.toList());
        if (!nonReferenceTypes.isEmpty()) {
            throw new IllegalArgumentException("markers cannot contain non-reference types. Found: "
                    + nonReferenceTypes);
        }
        return markers.stream()
                .map(typeMapper::getClassName)
                .map(ClassName.class::cast)
                .map(AnnotationSpec::builder)
                .map(AnnotationSpec.Builder::build)
                .collect(Collectors.toList());
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

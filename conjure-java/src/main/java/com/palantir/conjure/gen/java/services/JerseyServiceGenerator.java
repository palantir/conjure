/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.types.JerseyReturnTypeClassNameVisitor;
import com.palantir.conjure.gen.java.types.TypeMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class JerseyServiceGenerator implements ServiceGenerator {

    @Override
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports) {
        TypeMapper typeMapper =
                new TypeMapper(conjureDefinition.types(), imports);
        TypeMapper returnTypeMapper =
                new TypeMapper(conjureDefinition.types(), imports, JerseyReturnTypeClassNameVisitor::new);
        return conjureDefinition.services().entrySet().stream()
                .map(entry -> generateService(entry.getKey(), entry.getValue(), typeMapper, returnTypeMapper))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(String serviceName, ServiceDefinition serviceDefinition,
            TypeMapper typeMapper, TypeMapper returnTypeMapper) {
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
                        serviceDefinition.defaultAuth(), typeMapper, returnTypeMapper))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDefinition.conjurePackage().name(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec generateServiceMethod(
            String endpointName,
            EndpointDefinition endpointDef,
            AuthDefinition defaultAuth,
            TypeMapper typeMapper,
            TypeMapper returnTypeMapper) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(httpMethodToClassName(endpointDef.http().method()))
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", endpointDef.http().path())
                        .build())
                .addParameters(createServiceMethodParameters(endpointDef, defaultAuth, typeMapper));

        if (endpointDef.returns().map(type -> type instanceof BinaryType).orElse(false)) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Produces"))
                    .addMember("value", "$T.APPLICATION_OCTET_STREAM", ClassName.get("javax.ws.rs.core", "MediaType"))
                    .build());
        }

        endpointDef.deprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(
                ClassName.get("java.lang", "Deprecated")));

        ServiceGenerator.getJavaDoc(endpointDef).ifPresent(
                content -> methodBuilder.addJavadoc("$L", content.toString()));

        endpointDef.returns().ifPresent(type -> methodBuilder.returns(returnTypeMapper.getClassName(type)));

        return methodBuilder.build();
    }

    private static List<ParameterSpec> createServiceMethodParameters(
            EndpointDefinition endpointDef,
            AuthDefinition defaultAuth,
            TypeMapper typeMapper) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();

        AuthDefinition auth = endpointDef.auth().orElse(defaultAuth);
        createAuthParameter(auth).ifPresent(parameterSpecs::add);

        if (endpointDef.argsWithAutoDefined().isPresent()) {
            Set<String> pathArgs = endpointDef.http().pathArgs();
            endpointDef.argsWithAutoDefined().get().forEach((name, def) -> {
                parameterSpecs.add(createServiceMethodParameterArg(typeMapper, pathArgs, name, def));
            });
        }
        return ImmutableList.copyOf(parameterSpecs);
    }

    private static ParameterSpec createServiceMethodParameterArg(
            TypeMapper typeMapper,
            Set<String> pathArgs,
            String argumentName,
            ArgumentDefinition def) {
        ParameterSpec.Builder param = ParameterSpec.builder(typeMapper.getClassName(def.type()), argumentName);
        getParamTypeAnnotation(argumentName, def, pathArgs).ifPresent(param::addAnnotation);

        param.addAnnotations(createMarkers(typeMapper, def.markers()));
        return param.build();
    }

    private static Optional<ParameterSpec> createAuthParameter(AuthDefinition auth) {
        ClassName annotationClassName;
        ClassName tokenClassName;
        String paramName;
        switch (auth.type()) {
            case HEADER:
                annotationClassName = ClassName.get("javax.ws.rs", "HeaderParam");
                tokenClassName = ClassName.get("com.palantir.tokens.auth", "AuthHeader");
                paramName = "authHeader";
                break;
            case COOKIE:
                annotationClassName = ClassName.get("javax.ws.rs", "CookieParam");
                tokenClassName = ClassName.get("com.palantir.tokens.auth", "BearerToken");
                paramName = "token";
                break;
            case NONE:
            default:
                return Optional.empty();
        }
        return Optional.of(
                ParameterSpec.builder(tokenClassName, paramName)
                        .addAnnotation(AnnotationSpec.builder(annotationClassName)
                                .addMember("value", "$S", auth.id()).build())
                        .build());
    }

    private static Optional<AnnotationSpec> getParamTypeAnnotation(
            String argName,
            ArgumentDefinition def,
            Set<String> pathArgs) {
        final String annotationType;
        switch (def.paramType()) {
            case PATH:
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

    private static Set<AnnotationSpec> createMarkers(TypeMapper typeMapper, Set<ConjureType> markers) {
        checkArgument(markers.stream().allMatch(type -> type instanceof ReferenceType),
                "Markers must refer to reference types.");
        return markers.stream()
                .map(typeMapper::getClassName)
                .map(ClassName.class::cast)
                .map(AnnotationSpec::builder)
                .map(AnnotationSpec.Builder::build)
                .collect(Collectors.toSet());
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

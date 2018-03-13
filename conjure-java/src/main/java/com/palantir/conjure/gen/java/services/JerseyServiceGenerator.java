/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ParameterId;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.types.JerseyMethodTypeClassNameVisitor;
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

    private final Set<ExperimentalFeatures> experimentalFeatures;

    public JerseyServiceGenerator(Set<ExperimentalFeatures> experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

    @Override
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper returnTypeMapper = new TypeMapper(
                conjureDefinition.types(),
                types -> new JerseyReturnTypeClassNameVisitor(types, experimentalFeatures));
        TypeMapper methodTypeMapper = new TypeMapper(conjureDefinition.types(), JerseyMethodTypeClassNameVisitor::new);
        return conjureDefinition.services().stream()
                .map(serviceDef -> generateService(serviceDef, returnTypeMapper, methodTypeMapper))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(ServiceDefinition serviceDefinition,
            TypeMapper returnTypeMapper, TypeMapper methodTypeMapper) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceDefinition.serviceName().name())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Consumes"))
                        .addMember("value", "$T.APPLICATION_JSON", ClassName.get("javax.ws.rs.core", "MediaType"))
                        .build())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Produces"))
                        .addMember("value", "$T.APPLICATION_JSON", ClassName.get("javax.ws.rs.core", "MediaType"))
                        .build())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", "/")
                        .build())
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(JerseyServiceGenerator.class));

        serviceDefinition.docs().ifPresent(docs ->
                serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs.value(), "\n")));

        serviceBuilder.addMethods(serviceDefinition.endpoints().stream()
                .map(endpoint -> generateServiceMethod(endpoint, returnTypeMapper, methodTypeMapper))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDefinition.serviceName().conjurePackage().name(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec generateServiceMethod(
            EndpointDefinition endpointDef,
            TypeMapper returnTypeMapper,
            TypeMapper methodTypeMapper) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointDef.endpointName().name())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(httpMethodToClassName(endpointDef.http().method()))
                .addParameters(createServiceMethodParameters(endpointDef, methodTypeMapper));

        // @Path("") is invalid in Feign JaxRs and equivalent to absent on an endpoint method
        if (!endpointDef.http().path().withoutLeadingSlash().isEmpty()) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", endpointDef.http().path().withoutLeadingSlash())
                        .build());
        }

        if (experimentalFeatures.contains(ExperimentalFeatures.DangerousGothamMethodMarkers)) {
            methodBuilder.addAnnotations(createMarkers(methodTypeMapper, endpointDef.markers()));
        }
        if (endpointDef.returns().map(type -> type instanceof BinaryType).orElse(false)) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Produces"))
                    .addMember("value", "$T.APPLICATION_OCTET_STREAM", ClassName.get("javax.ws.rs.core", "MediaType"))
                    .build());
        }

        boolean consumesTypeIsBinary = endpointDef.args().stream()
                .anyMatch(arg -> arg.type() instanceof BinaryType && arg.paramType().equals(
                        ArgumentDefinition.ParamType.BODY));

        if (consumesTypeIsBinary) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Consumes"))
                    .addMember("value", "$T.APPLICATION_OCTET_STREAM", ClassName.get("javax.ws.rs.core", "MediaType"))
                    .build());
        }

        endpointDef.deprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(
                ClassName.get("java.lang", "Deprecated")));

        ServiceGenerator.getJavaDoc(endpointDef).ifPresent(
                content -> methodBuilder.addJavadoc("$L", content));

        endpointDef.returns().ifPresent(type -> methodBuilder.returns(returnTypeMapper.getClassName(type)));

        return methodBuilder.build();
    }

    private static List<ParameterSpec> createServiceMethodParameters(
            EndpointDefinition endpointDef,
            TypeMapper typeMapper) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();

        AuthDefinition auth = endpointDef.auth();
        createAuthParameter(auth).ifPresent(parameterSpecs::add);

        endpointDef.args().forEach(def -> {
            parameterSpecs.add(createServiceMethodParameterArg(typeMapper, def));
        });
        return ImmutableList.copyOf(parameterSpecs);
    }

    private static ParameterSpec createServiceMethodParameterArg(TypeMapper typeMapper, ArgumentDefinition def) {
        ParameterSpec.Builder param = ParameterSpec.builder(typeMapper.getClassName(def.type()), def.argName().name());
        getParamTypeAnnotation(def).ifPresent(param::addAnnotation);

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

    private static Optional<AnnotationSpec> getParamTypeAnnotation(ArgumentDefinition def) {
        AnnotationSpec.Builder annotationSpecBuilder;
        switch (def.paramType()) {
            case PATH:
                annotationSpecBuilder = AnnotationSpec.builder(ClassName.get("javax.ws.rs", "PathParam"))
                        .addMember("value", "$S", def.argName().name());
                break;
            case QUERY:
                annotationSpecBuilder = AnnotationSpec.builder(ClassName.get("javax.ws.rs", "QueryParam"))
                        .addMember("value", "$S",
                                def.paramId().map(ParameterId::name).orElse(def.argName().name()));
                break;
            case HEADER:
                annotationSpecBuilder = AnnotationSpec.builder(ClassName.get("javax.ws.rs", "HeaderParam"))
                        .addMember("value", "$S",
                                def.paramId().map(ParameterId::name).orElse(def.argName().name()));
                break;
            case BODY:
                /* no annotations for body parameters */
                return Optional.empty();
            default:
                throw new IllegalStateException("Unrecognized argument type: " + def.paramType());
        }

        return Optional.of(annotationSpecBuilder.build());
    }

    private static Set<AnnotationSpec> createMarkers(TypeMapper typeMapper, List<Type> markers) {
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

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.services.HttpPathWrapper;
import com.palantir.conjure.defs.types.AuthTypeVisitor;
import com.palantir.conjure.defs.types.ParameterTypeVisitor;
import com.palantir.conjure.defs.types.TypeVisitor;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.types.JerseyMethodTypeClassNameVisitor;
import com.palantir.conjure.gen.java.types.JerseyReturnTypeClassNameVisitor;
import com.palantir.conjure.gen.java.types.TypeMapper;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.AuthType;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.Type;
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
                conjureDefinition.getTypes(),
                types -> new JerseyReturnTypeClassNameVisitor(types, experimentalFeatures));
        TypeMapper methodTypeMapper = new TypeMapper(
                conjureDefinition.getTypes(), JerseyMethodTypeClassNameVisitor::new);
        return conjureDefinition.getServices().stream()
                .map(serviceDef -> generateService(serviceDef, returnTypeMapper, methodTypeMapper))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(ServiceDefinition serviceDefinition,
            TypeMapper returnTypeMapper, TypeMapper methodTypeMapper) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceDefinition.getServiceName().getName())
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

        serviceDefinition.getDocs().ifPresent(docs ->
                serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs.get(), "\n")));

        serviceBuilder.addMethods(serviceDefinition.getEndpoints().stream()
                .map(endpoint -> generateServiceMethod(endpoint, returnTypeMapper, methodTypeMapper))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDefinition.getServiceName().getPackage(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private MethodSpec generateServiceMethod(
            EndpointDefinition endpointDef,
            TypeMapper returnTypeMapper,
            TypeMapper methodTypeMapper) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointDef.getEndpointName().get())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(httpMethodToClassName(endpointDef.getHttpMethod().get().name()))
                .addParameters(createServiceMethodParameters(endpointDef, methodTypeMapper));

        // @Path("") is invalid in Feign JaxRs and equivalent to absent on an endpoint method
        String httpPath = HttpPathWrapper.withoutLeadingSlash(endpointDef.getHttpPath().get());
        if (!httpPath.isEmpty()) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", httpPath)
                        .build());
        }

        if (experimentalFeatures.contains(ExperimentalFeatures.DangerousGothamMethodMarkers)) {
            methodBuilder.addAnnotations(createMarkers(methodTypeMapper, endpointDef.getMarkers()));
        }

        if (endpointDef.getReturns().map(type -> type.accept(TypeVisitor.IS_BINARY)).orElse(false)) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Produces"))
                    .addMember("value", "$T.APPLICATION_OCTET_STREAM", ClassName.get("javax.ws.rs.core", "MediaType"))
                    .build());
        }

        boolean consumesTypeIsBinary = endpointDef.getArgs().stream()
                .anyMatch(arg -> arg.getType().accept(TypeVisitor.IS_BINARY)
                        && arg.getParamType().accept(ParameterTypeVisitor.IS_BODY));

        if (consumesTypeIsBinary) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Consumes"))
                    .addMember("value", "$T.APPLICATION_OCTET_STREAM", ClassName.get("javax.ws.rs.core", "MediaType"))
                    .build());
        }

        endpointDef.getDeprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(
                ClassName.get("java.lang", "Deprecated")));

        ServiceGenerator.getJavaDoc(endpointDef).ifPresent(
                content -> methodBuilder.addJavadoc("$L", content));

        endpointDef.getReturns().ifPresent(type -> methodBuilder.returns(returnTypeMapper.getClassName(type)));

        return methodBuilder.build();
    }

    private static List<ParameterSpec> createServiceMethodParameters(
            EndpointDefinition endpointDef,
            TypeMapper typeMapper) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();

        Optional<AuthType> auth = endpointDef.getAuth();
        createAuthParameter(auth).ifPresent(parameterSpecs::add);

        endpointDef.getArgs().forEach(def -> {
            parameterSpecs.add(createServiceMethodParameterArg(typeMapper, def));
        });
        return ImmutableList.copyOf(parameterSpecs);
    }

    private static ParameterSpec createServiceMethodParameterArg(TypeMapper typeMapper, ArgumentDefinition def) {
        ParameterSpec.Builder param = ParameterSpec.builder(
                typeMapper.getClassName(def.getType()), def.getArgName().get());
        getParamTypeAnnotation(def).ifPresent(param::addAnnotation);

        param.addAnnotations(createMarkers(typeMapper, def.getMarkers()));
        return param.build();
    }

    private static Optional<ParameterSpec> createAuthParameter(Optional<AuthType> auth) {
        ClassName annotationClassName;
        ClassName tokenClassName;
        String paramName;
        String tokenName;
        if (!auth.isPresent()) {
            return Optional.empty();
        } else if (auth.get().accept(AuthTypeVisitor.IS_HEADER)) {
            annotationClassName = ClassName.get("javax.ws.rs", "HeaderParam");
            tokenClassName = ClassName.get("com.palantir.tokens.auth", "AuthHeader");
            paramName = "authHeader";
            tokenName = "Authorization";
        } else if (auth.get().accept(AuthTypeVisitor.IS_COOKIE)) {
            annotationClassName = ClassName.get("javax.ws.rs", "CookieParam");
            tokenClassName = ClassName.get("com.palantir.tokens.auth", "BearerToken");
            paramName = "token";
            tokenName = auth.get().accept(AuthTypeVisitor.COOKIE).getCookieName();
        } else {
            throw new IllegalStateException("Unrecognized auth type: " + auth.get());
        }
        return Optional.of(
                ParameterSpec.builder(tokenClassName, paramName)
                        .addAnnotation(AnnotationSpec.builder(annotationClassName)
                                .addMember("value", "$S", tokenName).build())
                        .build());
    }

    private static Optional<AnnotationSpec> getParamTypeAnnotation(ArgumentDefinition def) {
        AnnotationSpec.Builder annotationSpecBuilder;
        ParameterType paramType = def.getParamType();
        if (paramType.accept(ParameterTypeVisitor.IS_PATH)) {
            annotationSpecBuilder = AnnotationSpec.builder(ClassName.get("javax.ws.rs", "PathParam"))
                    .addMember("value", "$S", def.getArgName().get());
        } else if (paramType.accept(ParameterTypeVisitor.IS_QUERY)) {
            ParameterId paramId = paramType.accept(ParameterTypeVisitor.QUERY).getParamId();
            annotationSpecBuilder = AnnotationSpec.builder(ClassName.get("javax.ws.rs", "QueryParam"))
                    .addMember("value", "$S", paramId.get());
        } else if (paramType.accept(ParameterTypeVisitor.IS_HEADER)) {
            ParameterId paramId = paramType.accept(ParameterTypeVisitor.HEADER).getParamId();
            annotationSpecBuilder = AnnotationSpec.builder(ClassName.get("javax.ws.rs", "HeaderParam"))
                    .addMember("value", "$S", paramId.get());
        } else if (paramType.accept(ParameterTypeVisitor.IS_BODY)) {
            /* no annotations for body parameters */
            return Optional.empty();
        } else {
            throw new IllegalStateException("Unrecognized argument type: " + def.getParamType());
        }

        return Optional.of(annotationSpecBuilder.build());
    }

    private static Set<AnnotationSpec> createMarkers(TypeMapper typeMapper, List<Type> markers) {
        checkArgument(markers.stream().allMatch(type -> type.accept(TypeVisitor.IS_REFERENCE)),
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

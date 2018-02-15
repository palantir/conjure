/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ParameterName;
import com.palantir.conjure.defs.services.PathDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.types.Retrofit2MethodTypeClassNameVisitor;
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

    private static final ClassName COMPLETABLE_FUTURE_TYPE = ClassName.get("java.util.concurrent", "CompletableFuture");
    private static final ClassName CALL_TYPE = ClassName.get("retrofit2", "Call");

    private static final Logger log = LoggerFactory.getLogger(Retrofit2ServiceGenerator.class);

    private final Set<ExperimentalFeatures> experimentalFeatures;

    public Retrofit2ServiceGenerator(Set<ExperimentalFeatures> experimentalFeatures) {
        this.experimentalFeatures = ImmutableSet.copyOf(experimentalFeatures);
    }

    @Override
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper returnTypeMapper =
                new TypeMapper(conjureDefinition.types(), Retrofit2ReturnTypeClassNameVisitor::new);
        TypeMapper methodTypeMapper =
                new TypeMapper(conjureDefinition.types(), Retrofit2MethodTypeClassNameVisitor::new);
        return conjureDefinition.services().stream()
                .map(serviceDef -> generateService(serviceDef, returnTypeMapper, methodTypeMapper))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(ServiceDefinition serviceDefinition,
            TypeMapper returnTypeMapper, TypeMapper methodTypeMapper) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceName(serviceDefinition))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(Retrofit2ServiceGenerator.class));

        if (serviceDefinition.docs().isPresent()) {
            serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(serviceDefinition.docs().get(), "\n"));
        }

        serviceBuilder.addMethods(serviceDefinition.endpoints().entrySet().stream()
                .map(endpoint -> generateServiceMethod(
                        endpoint.getKey(),
                        endpoint.getValue(),
                        returnTypeMapper,
                        methodTypeMapper))
                .collect(Collectors.toList()));

        return JavaFile.builder(serviceDefinition.serviceName().conjurePackage().name(), serviceBuilder.build())
                .indent("    ")
                .build();
    }

    private String serviceName(ServiceDefinition serviceDefinition) {
        if (experimentalFeatures.contains(ExperimentalFeatures.DisambiguateRetrofitServices)) {
            return serviceDefinition.serviceName().name() + "Retrofit";
        } else {
            return serviceDefinition.serviceName().name();
        }
    }

    private ClassName getReturnType() {
        if (experimentalFeatures.contains(ExperimentalFeatures.RetrofitCompletableFutures)) {
            return COMPLETABLE_FUTURE_TYPE;
        } else {
            return CALL_TYPE;
        }
    }

    private MethodSpec generateServiceMethod(
            String endpointName,
            EndpointDefinition endpointDef,
            TypeMapper returnTypeMapper,
            TypeMapper methodTypeMapper) {
        Set<ParameterName> encodedPathArgs = extractEncodedPathArgs(endpointDef.http().path());
        PathDefinition endpointPathWithoutRegex = replaceEncodedPathArgs(endpointDef.http().path());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(AnnotationSpec.builder(httpMethodToClassName(endpointDef.http().method()))
                        .addMember("value", "$S", "." + endpointPathWithoutRegex)
                        .build());

        if (experimentalFeatures.contains(ExperimentalFeatures.DangerousGothamMethodMarkers)) {
            methodBuilder.addAnnotations(createMarkers(methodTypeMapper, endpointDef.markers()));
        }
        if (endpointDef.returns().map(type -> type instanceof BinaryType).orElse(false)) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Streaming")).build());
        }

        endpointDef.deprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(
                ClassName.get("java.lang", "Deprecated")));

        ServiceGenerator.getJavaDoc(endpointDef).ifPresent(
                content -> methodBuilder.addJavadoc("$L", content));

        if (endpointDef.returns().isPresent()) {
            methodBuilder.returns(
                    ParameterizedTypeName.get(getReturnType(),
                            returnTypeMapper.getClassName(endpointDef.returns().get()).box()));
        } else {
            methodBuilder.returns(ParameterizedTypeName.get(getReturnType(), ClassName.get(Void.class)));
        }

        getAuthParameter(endpointDef.auth()).ifPresent(methodBuilder::addParameter);

        methodBuilder.addParameters(endpointDef.args().entrySet().stream()
                .map(arg -> createEndpointParameter(
                        methodTypeMapper, encodedPathArgs, arg.getKey(), arg.getValue()))
                .collect(Collectors.toList()));

        return methodBuilder.build();
    }

    private Set<ParameterName> extractEncodedPathArgs(PathDefinition path) {
        Pattern pathArg = Pattern.compile("\\{([^\\}]+)\\}");
        Matcher matcher = pathArg.matcher(path.toString());
        ImmutableSet.Builder<ParameterName> encodedArgs = ImmutableSet.builder();
        while (matcher.find()) {
            String arg = matcher.group(1);
            if (arg.contains(":")) {
                // Strip everything after first colon
                encodedArgs.add(ParameterName.of(arg.substring(0, arg.indexOf(':'))));
            }
        }
        return encodedArgs.build();
    }

    private PathDefinition replaceEncodedPathArgs(PathDefinition path) {
        List<String> newSegments = Lists.newArrayList();
        Pattern pattern = Pattern.compile("\\{([^:]+):(.*)}");
        for (String segment : path.path().getSegments()) {
            Matcher matcher = pattern.matcher(segment);
            if (matcher.matches()) {
                newSegments.add("{" + matcher.group(1) + "}");
            } else {
                newSegments.add(segment);
            }
        }
        return PathDefinition.of("/" + Joiner.on("/").join(newSegments));
    }

    private ParameterSpec createEndpointParameter(
            TypeMapper methodTypeMapper,
            Set<ParameterName> encodedPathArgs,
            ParameterName paramKey,
            ArgumentDefinition def) {
        ParameterSpec.Builder param = ParameterSpec.builder(
                methodTypeMapper.getClassName(def.type()),
                paramKey.name());

        switch (def.paramType()) {
            case PATH:
                ParameterName pathParamKey = def.paramId();
                AnnotationSpec.Builder builder = AnnotationSpec.builder(ClassName.get("retrofit2.http", "Path"))
                        .addMember("value", "$S", pathParamKey.name());
                if (encodedPathArgs.contains(pathParamKey)) {
                    builder.addMember("encoded", "$L", true);
                }
                param.addAnnotation(builder.build());
                break;
            case QUERY:
                param.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Query"))
                        .addMember("value", "$S", def.paramId())
                        .build());
                break;
            case HEADER:
                param.addAnnotation(AnnotationSpec.builder(ClassName.get("retrofit2.http", "Header"))
                        .addMember("value", "$S", def.paramId())
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

    private Optional<ParameterSpec> getAuthParameter(AuthDefinition auth) {
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

/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthType;
import com.palantir.conjure.defs.services.BodyParameterType;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.EndpointName;
import com.palantir.conjure.defs.services.HeaderAuthType;
import com.palantir.conjure.defs.services.HeaderParameterType;
import com.palantir.conjure.defs.services.HttpPath;
import com.palantir.conjure.defs.services.PathParameterType;
import com.palantir.conjure.defs.services.QueryParameterType;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.gen.java.types.TypeMapper;
import com.palantir.dialogue.Call;
import com.palantir.dialogue.Calls;
import com.palantir.dialogue.Channel;
import com.palantir.dialogue.Deserializer;
import com.palantir.dialogue.Deserializers;
import com.palantir.dialogue.DialogueOkHttpErrorDecoder;
import com.palantir.dialogue.Endpoint;
import com.palantir.dialogue.Exceptions;
import com.palantir.dialogue.HttpMethod;
import com.palantir.dialogue.OkHttpErrorDecoder;
import com.palantir.dialogue.PathTemplate;
import com.palantir.dialogue.Request;
import com.palantir.dialogue.Serializer;
import com.palantir.dialogue.Serializers;
import com.palantir.tokens.auth.AuthHeader;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.uri.internal.UriTemplateParser;

// TODO(rfink): Add unit tests for misc edge cases, e.g.: docs/no-docs, auth/no-auth, binary return type.
public final class DialogClientGenerator implements ServiceGenerator {

    @Override
    public Set<JavaFile> generate(ConjureDefinition conjureDefinition) {
        TypeMapper parameterTypes = new TypeMapper(conjureDefinition.types(),
                types -> new ClassVisitor(types, ClassVisitor.Mode.PARAMETER));
        TypeMapper returnTypes = new TypeMapper(conjureDefinition.types(),
                types -> new ClassVisitor(types, ClassVisitor.Mode.RETURN_VALUE));
        TypeAwareGenerator generator = new TypeAwareGenerator(parameterTypes, returnTypes);
        return conjureDefinition.services().stream()
                .flatMap(serviceDef -> generator.service(serviceDef).stream())
                .collect(Collectors.toSet());
    }

    // TODO(rfink): Split into separate classes: endpoint, interface, impl.
    private static final class TypeAwareGenerator {
        private static final String AUTH_HEADER_PARAM_NAME = "authHeader";
        private static final String MAPPER_FIELD_NAME = "mapper";
        private static final String REQUEST_VAR_NAME = "_request";
        private final TypeMapper parameterTypes;
        private final TypeMapper returnTypes;

        private TypeAwareGenerator(TypeMapper parameterTypes, TypeMapper returnTypes) {
            this.parameterTypes = parameterTypes;
            this.returnTypes = returnTypes;
        }

        private Set<JavaFile> service(ServiceDefinition def) {
            return ImmutableSet.of(blockingApi(def), client(def));
        }

        private JavaFile blockingApi(ServiceDefinition def) {
            TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceClassName("Blocking", def))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(DialogClientGenerator.class));

            def.docs().ifPresent(docs ->
                    serviceBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs.value(), "\n")));

            serviceBuilder.addMethods(def.endpoints().stream()
                    .map(this::blockingApiMethod)
                    .collect(Collectors.toList()));

            return JavaFile.builder(def.serviceName().conjurePackage().name(), serviceBuilder.build()).build();
        }

        private MethodSpec blockingApiMethod(EndpointDefinition endpointDef) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(endpointDef.endpointName().name())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameters(methodParams(endpointDef));

            endpointDef.deprecated().ifPresent(deprecatedDocsValue -> methodBuilder.addAnnotation(Deprecated.class));
            ServiceGenerator.getJavaDoc(endpointDef).ifPresent(content -> methodBuilder.addJavadoc("$L", content));
            endpointDef.returns().ifPresent(type -> methodBuilder.returns(returnTypes.getClassName(type)));

            return methodBuilder.build();
        }

        private JavaFile client(ServiceDefinition def) {
            ClassName className = serviceClassName("Dialogue", def);
            TypeSpec.Builder serviceBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(DialogClientGenerator.class));

            serviceBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());
            serviceBuilder.addMethod(blockingClient(def));
            serviceBuilder.addField(FieldSpec.builder(ObjectMapper.class, "mapper")
                    .initializer("$T.newClientObjectMapper()",
                            // TODO(rfink): Stop relying on http-remoting.
                            ClassName.get("com.palantir.remoting3.ext.jackson", "ObjectMappers"))
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .build());
            serviceBuilder.addFields(def.endpoints().stream()
                    .map(endpoint -> endpoint(endpoint, className))
                    .collect(Collectors.toList()));

            return JavaFile.builder(def.serviceName().conjurePackage().name(), serviceBuilder.build()).build();
        }

        private FieldSpec endpoint(EndpointDefinition def, ClassName parentClassName) {
            Optional<ArgumentDefinition> bodyParam = def.args().stream()
                    .filter(arg -> arg.paramType() instanceof BodyParameterType)
                    .findAny();
            TypeName bodyType = bodyParam
                    .map(arg -> parameterTypes.getClassName(arg.type()))
                    .orElse(TypeName.VOID)
                    .box();
            TypeName returnType = def.returns().map(returnTypes::getClassName).orElse(TypeName.VOID).box();
            ParameterizedTypeName endpointType =
                    ParameterizedTypeName.get(ClassName.get(Endpoint.class), bodyType, returnType);

            TypeSpec endpointClass = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(endpointType)
                    .addField(FieldSpec.builder(
                            TypeName.get(PathTemplate.class), "pathTemplate", Modifier.PRIVATE, Modifier.FINAL)
                            .initializer(CodeBlock.builder().add(pathTemplateInitializer(def.httpPath())).build())
                            .build())
                    // TODO(rfink): These fields cannot be static. Does this matter? Should we make these real
                    // instead of anonymous classes?
                    // TODO(rfink): Decide whether we want to initialize these lazily.
                    .addField(FieldSpec.builder(
                            ParameterizedTypeName.get(ClassName.get(Serializer.class), bodyType),
                            "serializer", Modifier.PRIVATE, Modifier.FINAL)
                            .initializer(serializerForType(def, parentClassName, bodyParam))
                            .build())
                    .addField(FieldSpec.builder(
                            ParameterizedTypeName.get(ClassName.get(Deserializer.class), returnType),
                            "deserializer", Modifier.PRIVATE, Modifier.FINAL)
                            .initializer(deserializeForType(def.returns(), def.endpointName(), parentClassName))
                            .build())
                    .addMethod(MethodSpec.methodBuilder("renderPath")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(String.class)
                            .addParameter(ParameterizedTypeName.get(Map.class, String.class, String.class), "params")
                            .addCode("return pathTemplate.fill(params);")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("httpMethod")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(HttpMethod.class)
                            .addCode(CodeBlock.builder().add("return $T.$L;", HttpMethod.class,
                                    def.httpMethod().name()).build())
                            .build())
                    .addMethod(MethodSpec.methodBuilder("requestSerializer")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ParameterizedTypeName.get(ClassName.get(Serializer.class), bodyType))
                            .addCode("return serializer;")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("responseDeserializer")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ParameterizedTypeName.get(ClassName.get(Deserializer.class), returnType))
                            .addCode("return deserializer;")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("errorDecoder")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(OkHttpErrorDecoder.class)
                            .addCode(CodeBlock.builder()
                                    .add("return $T.INSTANCE;", DialogueOkHttpErrorDecoder.class).build())
                            .build())
                    .build();

            return FieldSpec.builder(
                    endpointType,
                    def.endpointName().name(),
                    Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer(CodeBlock.builder().add("$L", endpointClass).build())
                    .build();
        }

        private CodeBlock serializerForType(
                EndpointDefinition def, ClassName parentClassName, Optional<ArgumentDefinition> bodyParam) {
            return bodyParam.isPresent()
                    ? CodeBlock.builder()
                    .add("$T.jackson($S, $L.$L);",
                            Serializers.class, def.endpointName().name(), parentClassName, MAPPER_FIELD_NAME)
                    .build()
                    : CodeBlock.builder().add("$T.failing();", Serializers.class).build();
        }

        private CodeBlock deserializeForType(
                Optional<Type> returnType, EndpointName endpointName, ClassName parentClassName) {
            if (!returnType.isPresent()) {
                // No return value: use "empty" deserializer.
                return CodeBlock.builder()
                        .add("$T.empty($S);", Deserializers.class, endpointName.name())
                        .build();
            } else {
                // For "binary" conjure return type, use passthrough deserializer, otherwise use Jackson deserializer
                CodeBlock jackson = CodeBlock.builder()
                        .add("$T.jackson($S, $L.$L, $L);",
                                Deserializers.class,
                                endpointName.name(),
                                parentClassName,
                                MAPPER_FIELD_NAME,
                                TypeSpec.anonymousClassBuilder("")
                                        .addSuperinterface(ParameterizedTypeName.get(
                                                ClassName.get(TypeReference.class),
                                                returnTypes.getClassName(returnType.get()).box()))
                                        .build())
                        .build();
                return returnType.get().visit(
                        new FixedDefaultConjureTypeVisitor<CodeBlock>(jackson) {
                            @Override
                            public CodeBlock visitBinary(BinaryType type) {
                                return CodeBlock.builder()
                                        .add("$T.passthrough();", Deserializers.class)
                                        .build();
                            }
                        });
            }
        }

        // TODO(rfink): Integrate/consolidate with checking code in PathDefinition class
        private CodeBlock pathTemplateInitializer(HttpPath path) {
            UriTemplateParser uriTemplateParser = new UriTemplateParser(path.path().toString());
            String[] rawSegments = uriTemplateParser.getNormalizedTemplate().split("/");
            List<CodeBlock> segments = new ArrayList<>();
            for (int i = 1; i < rawSegments.length; i++) {
                String segment = rawSegments[i];
                if (segment.isEmpty()) {
                    continue; // avoid empty segments; typically the first segment is empty
                }

                final String pattern;
                final String segmentName;
                if (segment.startsWith("{") && segment.endsWith("}")) {
                    pattern = "$T.variable($S)";
                    segmentName = segment.substring(1, segment.length() - 1);
                } else {
                    pattern = "$T.fixed($S)";
                    segmentName = segment;
                }
                segments.add(CodeBlock.builder()
                        .add(pattern, PathTemplate.Segment.class, segmentName).build());
            }

            return CodeBlock.builder().add("$T.of($T.of($L))",
                    com.palantir.dialogue.PathTemplate.class,
                    ImmutableList.class,
                    CodeBlock.join(segments, ",")).build();
        }

        private MethodSpec blockingClient(ServiceDefinition def) {
            TypeSpec.Builder impl = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(serviceClassName("Blocking", def));
            def.endpoints().forEach(endpoint -> impl.addMethod(
                    blockingClientImpl(serviceClassName("Dialogue", def).toString(), endpoint)));

            return MethodSpec.methodBuilder("blocking")
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .addJavadoc("Creates a synchronous/blocking client for a $L service.", def.serviceName().name())
                    .returns(serviceClassName("Blocking", def))
                    .addParameter(Channel.class, "channel")
                    .addCode(CodeBlock.builder().add("return $L;", impl.build()).build())
                    .build();
        }

        private MethodSpec blockingClientImpl(String serviceName, EndpointDefinition def) {
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(def.endpointName().name())
                    .addModifiers(Modifier.PUBLIC)
                    .addParameters(methodParams(def));
            def.returns().ifPresent(type -> methodBuilder.returns(returnTypes.getClassName(type)));

            // Validate inputs
            CodeBlock.Builder validateParams = CodeBlock.builder();
            def.args().stream()
                    .filter(param -> !parameterTypes.getClassName(param.type()).isPrimitive())
                    .forEach(param ->
                            validateParams.add("$T.checkNotNull($L, \"parameter $L must not be null\");",
                                    Preconditions.class, param.argName().name(), param.argName().name()));

            // Construct request
            CodeBlock.Builder request = CodeBlock.builder();
            List<ArgumentDefinition> bodyParams = def.args().stream()
                    .filter(param -> param.paramType() instanceof BodyParameterType)
                    .collect(Collectors.toList());
            Optional<ArgumentDefinition> bodyParam = Optional.ofNullable(Iterables.getOnlyElement(bodyParams, null));
            TypeName bodyType = bodyParam.map(p -> parameterTypes.getClassName(p.type()))
                    .orElse(TypeName.get(Void.class));
            // Add path/query/header/body parameters
            request.add("$T<$T> $L = $T.<$T>builder()",
                    Request.class, bodyType, REQUEST_VAR_NAME, Request.class, bodyType);
            def.args().forEach(param -> addParameter(request, param));
            // Add header parameter for HEADER authentication
            def.auth().ifPresent(auth -> {
                verifyAuthTypeIsHeader(auth);
                request.add(".putHeaderParams($S, $T.toString($L))",
                        HeaderAuthType.HEADER_NAME, Objects.class, AUTH_HEADER_PARAM_NAME);
            });
            request.add(".build();");

            // Perform call and return result
            CodeBlock.Builder call = CodeBlock.builder();
            TypeName returnType = def.returns().map(returnTypes::getClassName)
                    .orElse(TypeName.get(Void.class)).box();
            call.add("$T<$T> _call = channel.createCall($L.$L, $L);",
                    Call.class, returnType, serviceName, def.endpointName().name(), REQUEST_VAR_NAME);
            call.add("$T<$T> _response = $T.toFuture(_call);", ListenableFuture.class, returnType, Calls.class);
            call.beginControlFlow("try");
            if (def.returns().isPresent()) {
                call.add("return _response.get();");
            } else {
                call.add("_response.get();");
            }
            call.endControlFlow();
            call.beginControlFlow("catch($T _throwable)", Throwable.class);
            call.add("throw $T.unwrapExecutionException(_throwable);", Exceptions.class);
            call.endControlFlow();

            return methodBuilder
                    .addCode(validateParams.build())
                    .addCode(request.build())
                    .addCode(call.build())
                    .build();
        }

        private void addParameter(CodeBlock.Builder request, ArgumentDefinition param) {
            // TODO(rfink): Use native/primitive toString where possible instead of Objects#toString
            // This is to avoid object churn.
            if (param.paramType() instanceof PathParameterType) {
                request.add(".putPathParams($S, $T.toString($L))",
                        param.argName().name(), Objects.class, param.argName().name());
            } else if (param.paramType() instanceof HeaderParameterType) {
                request.add(".putHeaderParams($S, $T.toString($L))",
                        ((HeaderParameterType) param.paramType()).paramId(), Objects.class, param.argName().name());
            } else if (param.paramType() instanceof BodyParameterType) {
                request.add(".body($L)", param.argName().name());
            } else if (param.paramType() instanceof QueryParameterType) {
                request.add(".putQueryParams($S, $T.toString($L))",
                        ((QueryParameterType) param.paramType()).paramId(), Objects.class, param.argName().name());
            } else {
                throw new UnsupportedOperationException("Unknown parameter type: " + param.paramType());
            }
        }

        private List<ParameterSpec> methodParams(EndpointDefinition endpointDef) {
            List<ParameterSpec> parameterSpecs = new ArrayList<>();
            endpointDef.auth().ifPresent(auth -> parameterSpecs.add(authParam(auth)));

            endpointDef.args().forEach(def -> parameterSpecs.add(param(def)));
            return ImmutableList.copyOf(parameterSpecs);
        }

        private static void verifyAuthTypeIsHeader(AuthType authType) {
            if (!(authType instanceof HeaderAuthType)) {
                throw new UnsupportedOperationException("AuthType not supported by conjure-dialogue: " + authType);
            }
        }

        private ParameterSpec param(ArgumentDefinition def) {
            ParameterSpec.Builder param =
                    ParameterSpec.builder(parameterTypes.getClassName(def.type()), def.argName().name());

            param.addAnnotations(markers(def.markers()));
            return param.build();
        }

        private static ParameterSpec authParam(AuthType auth) {
            verifyAuthTypeIsHeader(auth);
            return ParameterSpec.builder(AuthHeader.class, AUTH_HEADER_PARAM_NAME).build();
        }

        private Set<AnnotationSpec> markers(List<Type> markers) {
            checkArgument(markers.stream().allMatch(type -> type instanceof ReferenceType),
                    "Markers must refer to reference types.");
            return markers.stream()
                    .map(parameterTypes::getClassName)
                    .map(ClassName.class::cast)
                    .map(AnnotationSpec::builder)
                    .map(AnnotationSpec.Builder::build)
                    .collect(Collectors.toSet());
        }

        private static ClassName serviceClassName(@Nullable String prefix, ServiceDefinition def) {
            String simpleName = Strings.nullToEmpty(prefix) + def.serviceName().name();
            return ClassName.get(def.serviceName().conjurePackage().name(), simpleName);
        }
    }
}

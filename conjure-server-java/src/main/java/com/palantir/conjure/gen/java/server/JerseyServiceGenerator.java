/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.server;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.services.ArgumentDefinition;
import com.palantir.conjure.defs.services.AuthDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SetType;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;

public final class JerseyServiceGenerator {

    private final ConjureDefinition conjure;
    private final TypesDefinition types;

    public JerseyServiceGenerator(ConjureDefinition conjure) {
        this.conjure = conjure;
        this.types = conjure.types();
    }

    public Set<JavaFile> generateTypes() {
        return types.definitions().objects().entrySet().stream()
                .map(entry -> generateType(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public void emit(File dir) {
        Stream.concat(generateTypes().stream(), generateServices().stream()).forEach(t -> {
            try {
                t.writeTo(dir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private JavaFile generateType(String typeName, ObjectTypeDefinition typeDef) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        typeDef.docs().ifPresent(docs -> typeBuilder.addJavadoc("$L", withEndOfLine(docs)));

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = conjureTypeToClassName(types, entry.getValue().type());

            FieldSpec field = FieldSpec.builder(type, entry.getKey(),
                    Modifier.PRIVATE, Modifier.FINAL).build();

            MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + ucfirst(entry.getKey()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return this.$N", field)
                    .returns(type);

            entry.getValue().docs().ifPresent(docs -> getterBuilder.addJavadoc("$L", withEndOfLine(docs)));

            typeBuilder.addField(field)
                    .addMethod(getterBuilder.build());

            constructorBuilder.addParameter(type, entry.getKey())
                    .addStatement("this.$N = $N", field, entry.getKey());
        }

        typeBuilder.addMethod(constructorBuilder.build());

        return JavaFile.builder(typeDef.packageName().orElse(types.definitions().defaultPackage()), typeBuilder.build())
                .indent("    ")
                .build();
    }

    private static String ucfirst(String in) {
        return Character.toUpperCase(in.charAt(0)) + in.substring(1);
    }

    private static String withEndOfLine(String in) {
        if (in.endsWith("\n")) {
            return in;
        }
        return in + "\n";
    }

    public Set<JavaFile> generateServices() {
        return conjure.services().entrySet().stream()
                .map(entry -> generateService(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private JavaFile generateService(String serviceName, ServiceDefinition serviceDef) {
        TypeSpec.Builder serviceBuilder = TypeSpec.interfaceBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("javax.ws.rs", "Path"))
                        .addMember("value", "$S", serviceDef.basePath())
                        .build());

        serviceDef.docs().ifPresent(docs -> serviceBuilder.addJavadoc("$L", withEndOfLine(docs)));

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

        endpointDef.docs().ifPresent(docs -> methodBuilder.addJavadoc("$L", withEndOfLine(docs)));

        endpointDef.returns().ifPresent(type -> methodBuilder.returns(conjureTypeToClassName(types, type)));

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
                            conjureTypeToClassName(types, def.type()),
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
                            /* do nothing */
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
            case "POST":
                return ClassName.get("javax.ws.rs", "POST");
            default:
                throw new IllegalArgumentException("Unrecognized HTTP method: " + method);
        }
    }

    private static TypeName conjureTypeToClassName(TypesDefinition types, ConjureType type) {
        if (type instanceof OptionalType) {
            TypeName innerType = conjureTypeToClassName(types, ((OptionalType) type).itemType());
            return ParameterizedTypeName.get(ClassName.get(java.util.Optional.class), innerType);
        } else if (type instanceof SetType) {
            TypeName innerType = conjureTypeToClassName(types, ((SetType) type).itemType());
            return ParameterizedTypeName.get(ClassName.get(java.util.Set.class), innerType);
        } else if (type instanceof ListType) {
            TypeName innerType = conjureTypeToClassName(types, ((ListType) type).itemType());
            return ParameterizedTypeName.get(ClassName.get(java.util.List.class), innerType);
        } else if (type instanceof MapType) {
            MapType mapType = (MapType) type;
            return ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                    conjureTypeToClassName(types, mapType.keyType()),
                    conjureTypeToClassName(types, mapType.valueType()));
        } else if (type instanceof PrimitiveType) {
            return primtiveTypeToClassName((PrimitiveType) type);
        } else if (type instanceof ReferenceType) {
            return referenceTypeToClassName(types, (ReferenceType) type);
        } else {
            throw new IllegalStateException("Unexpected type " + type.getClass());
        }
    }

    private static TypeName referenceTypeToClassName(TypesDefinition types, ReferenceType refType) {
        ObjectTypeDefinition defType = types.definitions().objects().get(refType.type());
        if (defType != null) {
            String packageName = defType.packageName().orElse(types.definitions().defaultPackage());
            return ClassName.get(packageName, refType.type());
        } else {
            ExternalTypeDefinition depType = types.imports().get(refType.type());
            checkNotNull(depType, "Unable to resolve type %s", refType.type());
            return ClassName.bestGuess(depType.external().get("java"));
        }
    }

    private static TypeName primtiveTypeToClassName(PrimitiveType type) {
        switch (type) {
            case STRING:
                return ClassName.get(String.class);
            case DOUBLE:
                return ClassName.get(Double.class);
            case INTEGER:
                return ClassName.get(Integer.class);
            default:
                throw new IllegalStateException("Unknown primitive type: " + type);
        }
    }

}

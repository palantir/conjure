/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class ErrorGenerator {

    private ErrorGenerator() {}

    public static Set<JavaFile> generateErrorTypes(
            TypeMapper typeMapper,
            ConjurePackage conjurePackage,
            Map<TypeName, ErrorTypeDefinition> errorTypeNameToDef) {

        return splitErrorDefsByNamespace(errorTypeNameToDef)
                .entrySet()
                .stream()
                .map(entry -> generateErrorTypesForNamespace(
                        typeMapper, conjurePackage, entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private static Map<ErrorNamespace, Map<TypeName, ErrorTypeDefinition>> splitErrorDefsByNamespace(
            Map<TypeName, ErrorTypeDefinition> errorTypeNameToDef) {

        Map<ErrorNamespace, Map<TypeName, ErrorTypeDefinition>> namespacedErrorDefs = Maps.newHashMap();
        errorTypeNameToDef.entrySet().stream().forEach(entry -> {
            ErrorNamespace namespace = entry.getValue().namespace();
            namespacedErrorDefs.computeIfAbsent(namespace, key -> Maps.newHashMap());
            namespacedErrorDefs.get(namespace).put(entry.getKey(), entry.getValue());
        });
        return namespacedErrorDefs;
    }

    private static JavaFile generateErrorTypesForNamespace(
            TypeMapper typeMapper,
            ConjurePackage conjurePackage,
            ErrorNamespace namespace,
            Map<TypeName, ErrorTypeDefinition> errorTypeDefinitionMap) {

        ClassName className = errorTypesClassName(conjurePackage, namespace);

        // Generate ErrorType definitions
        Set<FieldSpec> fieldSpecs = errorTypeDefinitionMap.entrySet().stream().map(e -> {
            CodeBlock initializer = CodeBlock.of("ErrorType.create(ErrorType.Code.$L, \"$L:$L\")",
                    e.getValue().code().name(),
                    namespace.name(),
                    e.getKey().name());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(
                    ClassName.get(ErrorType.class),
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, e.getKey().name()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(initializer);
            e.getValue().docs().ifPresent(docs -> fieldSpecBuilder.addJavadoc(docs));
            return fieldSpecBuilder.build();
        }).collect(Collectors.toSet());

        // Generate ServiceException factory methods
        Set<MethodSpec> methodSpecs = errorTypeDefinitionMap.entrySet().stream().map(entry -> {
            String methodName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, entry.getKey().name());
            String typeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, entry.getKey().name());

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.get(ServiceException.class));

            methodBuilder.addCode("return new $T($L", ServiceException.class, typeName);
            entry.getValue().safeArgs().entrySet().stream().forEach(arg ->
                    processArg(typeMapper, methodBuilder, arg.getKey().name(), arg.getValue(), true));
            entry.getValue().unsafeArgs().entrySet().stream().forEach(arg ->
                    processArg(typeMapper, methodBuilder, arg.getKey().name(), arg.getValue(), false));
            methodBuilder.addCode(");");

            return methodBuilder.build();
        }).collect(Collectors.toSet());

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                .addMethod(privateConstructor())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(ErrorGenerator.class));

        return JavaFile.builder(conjurePackage.name(), typeBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static void processArg(TypeMapper typeMapper, MethodSpec.Builder methodBuilder,
            String argName, FieldDefinition argDefinition, boolean isSafe) {
        com.squareup.javapoet.TypeName argType = typeMapper.getClassName(argDefinition.type());
        methodBuilder.addParameter(argType, argName);
        Class<?> clazz = isSafe ? SafeArg.class : UnsafeArg.class;
        methodBuilder.addCode(",\n    $T.of($S, $L)", clazz, argName, argName);
        argDefinition.docs().ifPresent(docs ->
                methodBuilder.addJavadoc("@$L: $L", argName, StringUtils.appendIfMissing(docs, "\n")));
    }

    private static ClassName errorTypesClassName(ConjurePackage conjurePackage, ErrorNamespace namespace) {
        return ClassName.get(conjurePackage.name(), namespace.name() + "Errors");
    }

    private static MethodSpec privateConstructor() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
    }

}

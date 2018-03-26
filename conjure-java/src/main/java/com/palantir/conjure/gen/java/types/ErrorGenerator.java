/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.UnsafeArg;
import com.palantir.remoting.api.errors.ErrorType;
import com.palantir.remoting.api.errors.ServiceException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class ErrorGenerator {

    private ErrorGenerator() {}

    public static Set<JavaFile> generateErrorTypes(
            TypeMapper typeMapper, List<ErrorDefinition> errorTypeNameToDef) {

        return splitErrorDefsByNamespace(errorTypeNameToDef)
                .entrySet()
                .stream()
                .flatMap(entry ->
                        entry.getValue()
                                .entrySet()
                                .stream()
                                .map(innerEntry -> generateErrorTypesForNamespace(
                                        typeMapper,
                                        entry.getKey(),
                                        innerEntry.getKey(),
                                        innerEntry.getValue()))
                ).collect(Collectors.toSet());
    }

    private static Map<String, Map<ErrorNamespace, List<ErrorDefinition>>> splitErrorDefsByNamespace(
            List<ErrorDefinition> errorTypeNameToDef) {

        Map<String, Map<ErrorNamespace, List<ErrorDefinition>>> pkgToNamespacedErrorDefs =
                Maps.newHashMap();
        errorTypeNameToDef.stream().forEach(errorDef -> {
            String errorPkg = errorDef.getErrorName().getPackage();
            pkgToNamespacedErrorDefs.computeIfAbsent(errorPkg, key -> Maps.newHashMap());

            Map<ErrorNamespace, List<ErrorDefinition>> namespacedErrorDefs =
                    pkgToNamespacedErrorDefs.get(errorPkg);
            ErrorNamespace namespace = errorDef.getNamespace();
            // TODO(rfink): Use Multimap?
            namespacedErrorDefs.computeIfAbsent(namespace, key -> new ArrayList<>());
            namespacedErrorDefs.get(namespace).add(errorDef);
        });
        return pkgToNamespacedErrorDefs;
    }

    private static JavaFile generateErrorTypesForNamespace(
            TypeMapper typeMapper,
            String conjurePackage,
            ErrorNamespace namespace,
            List<ErrorDefinition> errorTypeDefinitions) {

        ClassName className = errorTypesClassName(conjurePackage, namespace);

        // Generate ErrorType definitions
        List<FieldSpec> fieldSpecs = errorTypeDefinitions.stream().map(errorDef -> {
            CodeBlock initializer = CodeBlock.of("ErrorType.create(ErrorType.Code.$L, \"$L:$L\")",
                    errorDef.getCode().get(),
                    namespace.get(),
                    errorDef.getErrorName().getName());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(
                    ClassName.get(ErrorType.class),
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, errorDef.getErrorName().getName()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(initializer);
            errorDef.getDocs().ifPresent(docs -> fieldSpecBuilder.addJavadoc(docs.get()));
            return fieldSpecBuilder.build();
        }).collect(Collectors.toList());

        // Generate ServiceException factory methods
        List<MethodSpec> methodSpecs = errorTypeDefinitions.stream()
                .map(entry -> {
                    MethodSpec withoutCause = generateExceptionFactory(typeMapper, entry, false);
                    MethodSpec withCause = generateExceptionFactory(typeMapper, entry, true);
                    return Stream.of(withoutCause, withCause);
                })
                .flatMap(Function.identity())
                .collect(Collectors.toList());

        // Generate ServiceException factory check methods
        List<MethodSpec> checkMethodSpecs = errorTypeDefinitions.stream().map(entry -> {
            String exceptionMethodName = CaseFormat.UPPER_CAMEL.to(
                    CaseFormat.LOWER_CAMEL, entry.getErrorName().getName());
            String methodName = "throwIf" + entry.getErrorName().getName();

            String shouldThrowVar = "shouldThrow";

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(TypeName.BOOLEAN, shouldThrowVar);

            methodBuilder.addJavadoc("Throws a {@link ServiceException} of type $L when {@code $L} is true.\n",
                    entry.getErrorName().getName(), shouldThrowVar);
            methodBuilder.addJavadoc("@param $L $L\n", shouldThrowVar, "Cause the method to throw when true");
            Streams.concat(
                    entry.getSafeArgs().stream(),
                    entry.getUnsafeArgs().stream()).forEach(arg -> {
                        methodBuilder.addParameter(typeMapper.getClassName(arg.getType()), arg.getFieldName().get());
                        methodBuilder.addJavadoc("@param $L $L", arg.getFieldName().get(),
                                        StringUtils.appendIfMissing(
                                                arg.getDocs().map(Documentation::get).orElse(""), "\n"));
                    });

            methodBuilder.addCode("if ($L) {", shouldThrowVar);
            methodBuilder.addCode("throw $L;",
                    Expressions.localMethodCall(exceptionMethodName,
                            Streams.concat(
                                    entry.getSafeArgs().stream(),
                                    entry.getUnsafeArgs().stream()).map(arg -> arg.getFieldName().get())
                                    .collect(Collectors.toList())));
            methodBuilder.addCode("}");
            return methodBuilder.build();
        }).collect(Collectors.toList());

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                .addMethod(privateConstructor())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .addMethods(checkMethodSpecs)
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(ErrorGenerator.class));

        return JavaFile.builder(conjurePackage, typeBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static MethodSpec generateExceptionFactory(
            TypeMapper typeMapper, ErrorDefinition entry, boolean withCause) {
        String methodName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, entry.getErrorName().getName());
        String typeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, entry.getErrorName().getName());

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(ServiceException.class));

        methodBuilder.addCode("return new $T($L", ServiceException.class, typeName);

        if (withCause) {
            methodBuilder.addParameter(Throwable.class, "cause");
            methodBuilder.addCode(", cause");
        }

        entry.getSafeArgs().stream().forEach(arg ->
                processArg(typeMapper, methodBuilder, arg, true));

        entry.getUnsafeArgs().stream().forEach(arg ->
                processArg(typeMapper, methodBuilder, arg, false));
        methodBuilder.addCode(");");

        return methodBuilder.build();
    }

    private static void processArg(
            TypeMapper typeMapper, MethodSpec.Builder methodBuilder, FieldDefinition argDefinition, boolean isSafe) {

        String argName = argDefinition.getFieldName().get();
        com.squareup.javapoet.TypeName argType = typeMapper.getClassName(argDefinition.getType());
        methodBuilder.addParameter(argType, argName);
        Class<?> clazz = isSafe ? SafeArg.class : UnsafeArg.class;
        methodBuilder.addCode(",\n    $T.of($S, $L)", clazz, argName, argName);
        argDefinition.getDocs().ifPresent(docs ->
                methodBuilder.addJavadoc("@param $L $L", argName, StringUtils.appendIfMissing(docs.get(), "\n")));
    }

    private static ClassName errorTypesClassName(String conjurePackage, ErrorNamespace namespace) {
        return ClassName.get(conjurePackage, namespace.get() + "Errors");
    }

    private static MethodSpec privateConstructor() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
    }

}

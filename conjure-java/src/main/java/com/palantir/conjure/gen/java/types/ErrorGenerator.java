/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class ErrorGenerator {

    private ErrorGenerator() {}

    public static Set<JavaFile> generateErrorTypes(
            TypeMapper typeMapper, List<ErrorTypeDefinition> errorTypeNameToDef) {

        return splitErrorDefsByNamespace(errorTypeNameToDef)
                .entrySet()
                .stream()
                .flatMap(entry ->
                        entry.getValue()
                                .entrySet()
                                .stream()
                                .map(innerEntry -> generateErrorTypesForNamespace(
                                        typeMapper,
                                        ConjurePackage.of(entry.getKey()),
                                        innerEntry.getKey(),
                                        innerEntry.getValue()))
                ).collect(Collectors.toSet());
    }

    private static Map<String, Map<ErrorNamespace, List<ErrorTypeDefinition>>> splitErrorDefsByNamespace(
            List<ErrorTypeDefinition> errorTypeNameToDef) {

        Map<String, Map<ErrorNamespace, List<ErrorTypeDefinition>>> pkgToNamespacedErrorDefs =
                Maps.newHashMap();
        errorTypeNameToDef.stream().forEach(errorDef -> {
            ConjurePackage errorPkg = errorDef.typeName().conjurePackage();
            pkgToNamespacedErrorDefs.computeIfAbsent(errorPkg.name(), key -> Maps.newHashMap());

            Map<ErrorNamespace, List<ErrorTypeDefinition>> namespacedErrorDefs =
                    pkgToNamespacedErrorDefs.get(errorPkg.name());
            ErrorNamespace namespace = errorDef.namespace();
            // TODO(rfink): Use Multimap?
            namespacedErrorDefs.computeIfAbsent(namespace, key -> new ArrayList<>());
            namespacedErrorDefs.get(namespace).add(errorDef);
        });
        return pkgToNamespacedErrorDefs;
    }

    private static JavaFile generateErrorTypesForNamespace(
            TypeMapper typeMapper,
            ConjurePackage conjurePackage,
            ErrorNamespace namespace,
            List<ErrorTypeDefinition> errorTypeDefinitionMap) {

        ClassName className = errorTypesClassName(conjurePackage, namespace);

        // Generate ErrorType definitions
        Set<FieldSpec> fieldSpecs = errorTypeDefinitionMap.stream().map(errorDef -> {
            CodeBlock initializer = CodeBlock.of("ErrorType.create(ErrorType.Code.$L, \"$L:$L\")",
                    errorDef.code().name(),
                    namespace.name(),
                    errorDef.typeName().name());
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(
                    ClassName.get(ErrorType.class),
                    CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, errorDef.typeName().name()),
                    Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(initializer);
            errorDef.docs().ifPresent(docs -> fieldSpecBuilder.addJavadoc(docs));
            return fieldSpecBuilder.build();
        }).collect(Collectors.toSet());

        // Generate ServiceException factory methods
        Set<MethodSpec> methodSpecs = errorTypeDefinitionMap.stream().map(entry -> {
            String methodName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, entry.typeName().name());
            String typeName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, entry.typeName().name());

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.get(ServiceException.class));

            methodBuilder.addCode("return new $T($L", ServiceException.class, typeName);
            entry.safeArgs().entrySet().stream().forEach(arg ->
                    processArg(typeMapper, methodBuilder, arg.getKey().name(), arg.getValue(), true));
            entry.unsafeArgs().entrySet().stream().forEach(arg ->
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
                methodBuilder.addJavadoc("@param $L $L", argName, StringUtils.appendIfMissing(docs, "\n")));
    }

    private static ClassName errorTypesClassName(ConjurePackage conjurePackage, ErrorNamespace namespace) {
        return ClassName.get(conjurePackage.name(), namespace.name() + "Errors");
    }

    private static MethodSpec privateConstructor() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
    }

}

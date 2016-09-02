/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Iterables;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.EnumValueDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class EnumGenerator {

    private EnumGenerator() {}

    public static JavaFile generateEnumType(
            TypesDefinition types,
            String defaultPackage,
            String typeName,
            EnumTypeDefinition typeDef) {
        String typePackage = typeDef.packageName().orElse(defaultPackage);
        ClassName thisClass = ClassName.get(typePackage, typeName);
        ClassName enumClass = ClassName.get(typePackage, typeName, "Value");

        TypeSpec.Builder wrapper = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addType(createEnum(typeDef.values()))
                .addField(enumClass, "value", Modifier.PRIVATE, Modifier.FINAL)
                .addField(ClassName.get(String.class), "string", Modifier.PRIVATE, Modifier.FINAL)
                .addFields(createConstants(typeDef.values(), thisClass, enumClass))
                .addMethod(createValueConstructor(enumClass))
                .addMethod(createStringConstructor(enumClass))
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(enumClass)
                        .addStatement("return this.value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("toString")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addAnnotation(JsonValue.class)
                        .returns(ClassName.get(String.class))
                        .addStatement("return this.string")
                        .build())
                .addMethod(createEquals(thisClass))
                .addMethod(createHashCode())
                .addMethod(createValueOf(thisClass, typeDef.values()));

        if (typeDef.docs().isPresent()) {
            wrapper.addJavadoc("$L", StringUtils.appendIfMissing(typeDef.docs().get(), "\n"));
        }

        return JavaFile.builder(typePackage, wrapper.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static Iterable<FieldSpec> createConstants(Set<EnumValueDefinition> values,
            ClassName thisClass, ClassName enumClass) {
        return Iterables.transform(values,
                v -> {
                    FieldSpec.Builder fieldSpec = FieldSpec.builder(thisClass, v.value(),
                            Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer(CodeBlock.of("new $1T($2T.$3N)", thisClass, enumClass, v.value()));
                    if (v.docs().isPresent()) {
                        fieldSpec.addJavadoc("$L", StringUtils.appendIfMissing(v.docs().get(), "\n"));
                    }
                    return fieldSpec.build();
                });
    }

    private static TypeSpec createEnum(Set<EnumValueDefinition> values) {
        TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder("Value")
                .addModifiers(Modifier.PUBLIC);
        for (EnumValueDefinition value : values) {
            TypeSpec.Builder enumClass = TypeSpec.anonymousClassBuilder("");
            if (value.docs().isPresent()) {
                enumClass.addJavadoc("$L", StringUtils.appendIfMissing(value.docs().get(), "\n"));
            }
            enumBuilder.addEnumConstant(value.value(), enumClass.build());
        }
        enumBuilder.addEnumConstant("UNKNOWN");

        return enumBuilder.build();
    }

    private static MethodSpec createValueConstructor(ClassName enumClass) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(enumClass, "value")
                .addStatement("this.value = value")
                .addStatement("this.string = value.name()")
                .build();
    }

    private static MethodSpec createStringConstructor(ClassName enumClass) {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.get(String.class), "string")
                .addStatement("this.value = $T.UNKNOWN", enumClass)
                .addStatement("this.string = string")
                .build();
    }

    private static MethodSpec createValueOf(ClassName thisClass, Set<EnumValueDefinition> values) {
        ParameterSpec param = ParameterSpec.builder(ClassName.get(String.class), "value").build();

        CodeBlock.Builder parser = CodeBlock.builder()
                .beginControlFlow("switch ($N)", param);
        for (EnumValueDefinition value : values) {
            parser.add("case $S:\n", value.value())
                    .indent()
                        .addStatement("return $L", value.value())
                    .unindent();
        }
        parser.add("default:\n")
                .indent()
                    .addStatement("return new $T($N)", thisClass, param)
                .unindent()
                .endControlFlow();

        return MethodSpec.methodBuilder("valueOf")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(thisClass)
                .addAnnotation(JsonCreator.class)
                .addParameter(param)
                .addStatement("$1T.requireNonNull($2N, \"$2N cannot be null\")", Objects.class, param)
                .addCode(parser.build())
                .build();
    }

    private static MethodSpec createEquals(TypeName thisClass) {
        ParameterSpec other = ParameterSpec.builder(TypeName.OBJECT, "other").build();
        return MethodSpec.methodBuilder("equals")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(other)
                .returns(TypeName.BOOLEAN)
                .addStatement("return (this == $1N) || ($1N instanceof $2T && this.string.equals((($2T) $1N).string))",
                        other, thisClass)
                .build();
    }

    private static MethodSpec createHashCode() {
        return MethodSpec.methodBuilder("hashCode")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.INT)
                .addStatement("return this.string.hashCode()")
                .build();
    }

}

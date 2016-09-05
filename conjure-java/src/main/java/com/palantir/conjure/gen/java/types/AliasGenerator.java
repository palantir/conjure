/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class AliasGenerator {

    private AliasGenerator() {}

    public static JavaFile generateAliasType(
            TypesDefinition types,
            String defaultPackage,
            String typeName,
            AliasTypeDefinition typeDef,
            Settings settings) {
        TypeMapper typeMapper = new TypeMapper(types, settings.optionalTypeStrategy());
        TypeName aliasType = typeMapper.getClassName(typeDef.alias());

        String typePackage = typeDef.packageName().orElse(defaultPackage);
        ClassName thisClass = ClassName.get(typePackage, typeName);

        TypeSpec.Builder spec = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(aliasType, "value", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(createConstructor(aliasType))
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(aliasType)
                        .addStatement("return value")
                        .build())
                .addMethod(MethodSpec.methodBuilder("toString")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addAnnotation(JsonValue.class)
                        .returns(aliasType)
                        .addCode(primitiveSafeToString(aliasType))
                        .build())
                .addMethod(MethodSpec.methodBuilder("equals")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(TypeName.OBJECT, "other")
                        .returns(TypeName.BOOLEAN)
                        .addCode(primitiveSafeEquality(thisClass, aliasType))
                        .build())
                .addMethod(MethodSpec.methodBuilder("hashCode")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.INT)
                        .addCode(primitiveSafeHashCode(aliasType))
                        .build())
                .addMethod(MethodSpec.methodBuilder("valueOf")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addAnnotation(JsonCreator.class)
                        .addParameter(aliasType, "value")
                        .returns(thisClass)
                        .addStatement("return new $T(value)", thisClass)
                        .build());

        if (typeDef.docs().isPresent()) {
            spec.addJavadoc("$L", StringUtils.appendIfMissing(typeDef.docs().get(), "\n"));
        }

        return JavaFile.builder(typePackage, spec.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static MethodSpec createConstructor(TypeName aliasType) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(aliasType, "value");
        if (!aliasType.isPrimitive()) {
            builder.addStatement("$T.requireNonNull(value, \"value cannot be null\")", Objects.class);
        }
        return builder
                .addStatement("this.value = value")
                .build();
    }

    private static CodeBlock primitiveSafeEquality(ClassName thisClass, TypeName fieldType) {
        if (fieldType.isPrimitive()) {
            return CodeBlocks.statement(
                    "return this == other || (other instanceof $1T && this.value == (($1T) other).value)",
                    thisClass);
        }
        return CodeBlocks.statement(
                "return this == other || (other instanceof $1T && this.value.equals((($1T) other).value))",
                thisClass);
    }

    private static CodeBlock primitiveSafeToString(TypeName fieldType) {
        if (fieldType.isPrimitive()) {
            return CodeBlocks.statement("return $T.valueOf(value)", String.class);
        }
        return CodeBlocks.statement("return value.toString()");
    }

    private static CodeBlock primitiveSafeHashCode(TypeName fieldType) {
        if (fieldType.isPrimitive()) {
            return CodeBlocks.statement("return $T.hashCode(value)", fieldType.box());
        }
        return CodeBlocks.statement("return value.hashCode()");
    }

}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collector;
import javax.lang.model.element.Modifier;

public final class MethodSpecs {

    public static MethodSpec createEquals(TypeName thisClass) {
        ParameterSpec other = ParameterSpec.builder(TypeName.OBJECT, "other").build();
        return MethodSpec.methodBuilder("equals")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(other)
                .returns(TypeName.BOOLEAN)
                .addStatement("return this == $1N || ($1N instanceof $2T && equalTo(($2T) $1N))",
                        other, thisClass)
                .build();
    }

    public static MethodSpec createEqualTo(TypeName thisClass, Collection<FieldSpec> fields) {
        CodeBlock equalsTo = createEqualsToStatement(fields);

        return MethodSpec.methodBuilder("equalTo")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(thisClass, "other")
                .returns(TypeName.BOOLEAN)
                .addStatement("return $L", equalsTo)
                .build();
    }

    public static MethodSpec createHashCode(Collection<FieldSpec> fields) {
        CodeBlock hashInput = CodeBlocks.of(
                fields.stream()
                        .map(MethodSpecs::createHashInput)
                        .collect(joining(CodeBlock.of(", "))));

        return MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return $1T.$2N($3L)", Objects.class, "hash", hashInput)
                .build();
    }

    public static MethodSpec createToString(String thisClassName, Collection<FieldSpec> fields) {
        CodeBlock returnStatement = CodeBlock.builder()
                .add("return new $T($S).append(\"{\")\n", StringBuilder.class, thisClassName)
                .indent()
                .indent()
                .add(CodeBlocks.of(fields.stream()
                        .map(f -> f.name)
                        .map(MethodSpecs::createAppendStatement)
                        .collect(joining(CodeBlock.of(".append(\", \")")))))
                .unindent()
                .add(".append(\"}\")\n")
                .addStatement(".toString()")
                .unindent()
                .build();

        return MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(String.class))
                .addCode(returnStatement)
                .build();
    }

    private static CodeBlock createAppendStatement(String fieldName) {
        return CodeBlock.builder()
                .add(".append($S)", fieldName)
                .add(".append(\": \")")
                .add(".append($N)", fieldName)
                .add("\n")
                .build();
    }

    private static CodeBlock createEqualsToStatement(Collection<FieldSpec> fields) {
        if (fields.isEmpty()) {
            return CodeBlock.of("$L", true);
        }

        return CodeBlocks.of(fields.stream()
                .map(MethodSpecs::createEqualsStatement)
                .collect(joining(CodeBlock.of(" && "))));
    }

    private static CodeBlock createEqualsStatement(FieldSpec field) {
        String thisField = "this." + field.name;
        String otherField = "other." + field.name;

        if (field.type.isPrimitive()) {
            return CodeBlock.of("$L == $L", thisField, otherField);
        } else if (field.type.equals(ClassName.get(ZonedDateTime.class))) {
            return CodeBlock.of("$L.isEqual($L)", thisField, otherField);
        }

        return CodeBlock.of("$L.equals($L)", thisField, otherField);
    }

    private static CodeBlock createHashInput(FieldSpec field) {
        if (field.type.equals(ClassName.get(ZonedDateTime.class))) {
            return CodeBlock.of("$N.toInstant()", field);
        }

        return CodeBlock.of("$N", field);
    }

    private static <T> Collector<T, ArrayList<T>, ArrayList<T>> joining(T delim) {
        return Collector.of(ArrayList::new,
                (list, element) -> {
                    if (!list.isEmpty()) {
                        list.add(delim);
                    }
                    list.add(element);
                },
                (list1, list2) -> {
                    if (!list1.isEmpty()) {
                        list1.add(delim);
                    }
                    list1.addAll(list2);
                    return list1;
                });
    }

    private MethodSpecs() {}

}

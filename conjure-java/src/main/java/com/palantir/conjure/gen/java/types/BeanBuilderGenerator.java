/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.collect.Iterables;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.gen.java.types.BeanGenerator.EnrichedField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public final class BeanBuilderGenerator {

    private BeanBuilderGenerator() {}

    public static TypeSpec generate(
            TypeMapper typeMapper,
            String defaultPackage,
            ClassName objectClass,
            ClassName builderClass,
            ObjectTypeDefinition typeDef) {
        Collection<EnrichedField> fields = createFields(typeMapper, typeDef.fields());
        Collection<FieldSpec> poetFields = EnrichedField.toPoetSpecs(fields);

        return TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(poetFields)
                .addMethod(createFromObject(builderClass, objectClass, fields))
                .addMethods(createSetters(builderClass, fields))
                .addMethod(createBuild(objectClass, poetFields))
                .build();
    }

    private static MethodSpec createFromObject(
            ClassName builderClass,
            ClassName objectClass,
            Collection<EnrichedField> fields) {
        return MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(objectClass, "other")
                .returns(builderClass)
                .addCode(CodeBlocks.of(Iterables.transform(fields, f -> setWithGetter(f.poetSpec().name))))
                .addStatement("return this")
                .build();
    }

    private static CodeBlock setWithGetter(String name) {
        return CodeBlocks.statement("$1N(other.$2N())", name, BeanGenerator.generateGetterName(name));
    }

    private static Collection<EnrichedField> createFields(TypeMapper typeMapper, Map<String, FieldDefinition> fields) {
        return fields.entrySet().stream()
                .map(e -> createField(typeMapper, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static EnrichedField createField(TypeMapper typeMapper, String jsonKey, FieldDefinition field) {
        FieldSpec.Builder spec = FieldSpec.builder(
                typeMapper.getClassName(field.type()),
                Fields.toSafeFieldName(jsonKey),
                Modifier.PRIVATE);
        if (field.type() instanceof ListType) {
            spec.initializer("new $T<>()", ArrayList.class);
        } else if (field.type() instanceof SetType) {
            spec.initializer("new $T<>()", LinkedHashSet.class);
        } else if (field.type() instanceof MapType) {
            spec.initializer("new $T<>()", LinkedHashMap.class);
        } else if (field.type() instanceof OptionalType) {
            spec.initializer(typeMapper.absentOptional());
        }
        return EnrichedField.of(jsonKey, field, spec.build());
    }

    private static Iterable<MethodSpec> createSetters(ClassName builderClass, Collection<EnrichedField> fields) {
        return Iterables.transform(fields, f -> createSetter(builderClass, f.poetSpec(), f.conjureDef().type()));
    }

    private static MethodSpec createSetter(ClassName builderClass, FieldSpec field, ConjureType type) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(field.type, field.name).build())
                .returns(builderClass)
                .beginControlFlow("if ($N == null)", field.name)
                    .addStatement("throw new $T(\"$N cannot be null\")", IllegalArgumentException.class, field.name)
                .endControlFlow()
                .addCode(typeAwareSet(field, type))
                .addStatement("return this").build();
    }

    private static CodeBlock typeAwareSet(FieldSpec spec, ConjureType type) {
        if (type instanceof ListType || type instanceof SetType) {
            return CodeBlocks.statement("this.$1N.addAll($1N)", spec.name);
        } else if (type instanceof MapType) {
            return CodeBlocks.statement("this.$1N.putAll($1N)", spec.name);
        } else {
            return CodeBlocks.statement("this.$1N = $1N", spec.name);
        }
    }

    private static MethodSpec createBuild(ClassName objectClass, Collection<FieldSpec> fields) {
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(objectClass)
                .addStatement("return new $L", Expressions.constructorCall(objectClass, fields))
                .build();
    }

}

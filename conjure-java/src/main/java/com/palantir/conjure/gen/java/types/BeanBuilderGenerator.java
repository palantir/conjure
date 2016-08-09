/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.gen.java.types.BeanGenerator.EnrichedField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public final class BeanBuilderGenerator {

    private BeanBuilderGenerator() {}

    public static TypeSpec generate(
            TypeMapper typeMapper,
            String defaultPackage,
            ClassName objectClass,
            ObjectTypeDefinition typeDef) {
        ClassName builderClass = ClassName.get(objectClass.packageName(), objectClass.simpleName(), "Builder");

        Collection<EnrichedField> fields = createFields(typeMapper, typeDef.fields());
        Collection<FieldSpec> poetFields = EnrichedField.toPoetSpecs(fields);

        return TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(poetFields)
                .addMethods(createSetters(builderClass, fields))
                .addMethod(createBuild(objectClass, poetFields))
                .build();
    }

    private static Collection<EnrichedField> createFields(TypeMapper typeMapper, Map<String, FieldDefinition> fields) {
        return fields.entrySet().stream()
                .map(e -> EnrichedField.of(e.getKey(), e.getValue(), FieldSpec.builder(
                            typeMapper.getClassName(e.getValue().type()),
                            Fields.toSafeFieldName(e.getKey()),
                            Modifier.PRIVATE)
                        .build()))
                .collect(Collectors.toList());
    }

    private static Collection<MethodSpec> createSetters(ClassName builderClass, Collection<EnrichedField> fields) {
        return fields.stream()
                .map(f -> createSetter(builderClass, f.poetSpec(), f.conjureDef().docs()))
                .collect(Collectors.toList());
    }

    private static MethodSpec createSetter(ClassName builderClass, FieldSpec field, Optional<String> docs) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(field.type, field.name).build())
                .returns(builderClass)
                .beginControlFlow("if ($N == null)", field.name)
                    .addStatement("throw new $T(\"$N cannot be null\")", IllegalArgumentException.class, field.name)
                .endControlFlow()
                .addStatement("this.$1N = $1N", field.name)
                .addStatement("return this")
                .build();
    }

    private static MethodSpec createBuild(ClassName objectClass, Collection<FieldSpec> fields) {
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(objectClass)
                .addStatement("return new $L", Expressions.constructorCall(objectClass, fields))
                .build();
    }

}

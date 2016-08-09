/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.collect.Lists;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import java.util.Map.Entry;
import javax.lang.model.element.Modifier;

public final class BeanBuilderGenerator {

    private BeanBuilderGenerator() {}

    public static TypeSpec generate(
            TypeMapper typeMapper,
            String defaultPackage,
            String typeName,
            ObjectTypeDefinition typeDef) {
        String typePackage = typeDef.packageName().orElse(defaultPackage);
        ClassName objectClass = ClassName.get(typePackage, typeName);
        ClassName builderClass = ClassName.get(typePackage, typeName, "Builder");

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        List<FieldSpec> fields = Lists.newArrayListWithCapacity(typeDef.fields().size());

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());
            String jsonName = entry.getKey();
            FieldSpec field = FieldSpec.builder(type, Fields.toSafeFieldName(jsonName), Modifier.PRIVATE).build();
            fields.add(field);

            typeBuilder.addField(field);

            typeBuilder.addMethod(MethodSpec.methodBuilder(field.name)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(field.type, field.name).build())
                    .returns(builderClass)
                    .beginControlFlow("if ($N == null)", field.name)
                        .addStatement("throw new $T(\"$N cannot be null\")", IllegalArgumentException.class, field.name)
                    .endControlFlow()
                    .addStatement("this.$1N = $1N", field.name)
                    .addStatement("return this")
                    .build());
        }

        typeBuilder.addMethod(MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(objectClass)
                .addStatement("return new $L", Expressions.constructorCall(objectClass, fields))
                .build());

        return typeBuilder.build();
    }

}

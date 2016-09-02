/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Map.Entry;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class ImmutablesJavaTypeGenerator implements TypeGenerator {

    private final Settings settings;

    public ImmutablesJavaTypeGenerator(Settings settings) {
        this.settings = settings;
    }

    @Override
    public JavaFile generateType(
            TypesDefinition types,
            String defaultPackage,
            String typeName,
            BaseObjectTypeDefinition typeDef) {
        if (typeDef instanceof ObjectTypeDefinition) {
            return generateImmutableType(types, defaultPackage, typeName, (ObjectTypeDefinition) typeDef);
        } else if (typeDef instanceof EnumTypeDefinition) {
            return EnumGenerator.generateEnumType(types, defaultPackage, typeName, (EnumTypeDefinition) typeDef,
                    settings);
        }
        throw new IllegalArgumentException("Unknown object definition type " + typeDef.getClass());
    }

    public JavaFile generateImmutableType(
            TypesDefinition types,
            String defaultPackage,
            String typeName,
            ObjectTypeDefinition typeDef) {
        TypeMapper typeMapper = new TypeMapper(types, settings.optionalTypeStrategy());
        String packageName = typeDef.packageName().orElse(types.definitions().defaultPackage());
        TypeSpec.Builder typeBuilder = TypeSpec.interfaceBuilder(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(JsonDeserialize.class)
                        .addMember("as", "$T.class", ClassName.get(packageName, "Immutable" + typeName)).build())
                .addAnnotation(AnnotationSpec.builder(JsonSerialize.class)
                        .addMember("as", "$T.class", ClassName.get(packageName, "Immutable" + typeName)).build())
                .addAnnotation(AnnotationSpec.builder(
                        ClassName.get("org.immutables.value", "Value", "Immutable")).build())
                .addAnnotation(AnnotationSpec.builder(
                        ClassName.get("org.immutables.value", "Value", "Style"))
                        .addMember("visibility", "$T.PACKAGE",
                                ClassName.get("org.immutables.value", "Value", "Style", "ImplementationVisibility"))
                        .build());

        if (settings.ignoreUnknownProperties()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
                    .addMember("ignoreUnknown", "$L", true)
                    .build());
        }

        typeDef.docs().ifPresent(docs -> typeBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());

            String fieldName = Fields.toSafeFieldName(entry.getKey());

            MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(
                    "get" + StringUtils.capitalize(fieldName))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(type);

            if (!fieldName.equals(entry.getKey())) {
                getterBuilder.addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", entry.getKey())
                        .build());
            }

            entry.getValue().docs().ifPresent(
                    docs -> getterBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

            typeBuilder.addMethod(getterBuilder.build());

        }

        return JavaFile.builder(packageName, typeBuilder.build())
                .indent("    ")
                .build();
    }

}

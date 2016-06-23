/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.StringCleanup;
import com.palantir.conjure.gen.java.TypeGenerator;
import com.palantir.conjure.gen.java.TypeMapper;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Map.Entry;
import javax.lang.model.element.Modifier;

public final class ImmutablesJavaTypeGenerator implements TypeGenerator {

    @Override
    public JavaFile generateType(
            TypesDefinition types,
            Settings settings,
            TypeMapper typeMapper,
            String defaultPackage,
            String typeName,
            ObjectTypeDefinition typeDef) {
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

        typeDef.docs().ifPresent(docs -> typeBuilder.addJavadoc("$L", StringCleanup.withEndOfLine(docs)));

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());

            MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + StringCleanup.ucfirst(entry.getKey()))
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(type);

            entry.getValue().docs().ifPresent(
                    docs -> getterBuilder.addJavadoc("$L", StringCleanup.withEndOfLine(docs)));

            typeBuilder.addMethod(getterBuilder.build());

        }

        return JavaFile.builder(packageName, typeBuilder.build())
                .indent("    ")
                .build();
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Map.Entry;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class BeanJavaTypeGenerator implements TypeGenerator {

    private final Settings settings;

    public BeanJavaTypeGenerator(Settings settings) {
        this.settings = settings;
    }

    @Override
    public JavaFile generateType(
            TypesDefinition types,
            String defaultPackage,
            String typeName,
            ObjectTypeDefinition typeDef) {
        TypeMapper typeMapper = new TypeMapper(types, settings.optionalTypeStrategy());
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        if (settings.ignoreUnknownProperties()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
                    .addMember("ignoreUnknown", "$L", true)
                    .build());
        }

        typeDef.docs().ifPresent(docs -> typeBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());

            FieldSpec field = FieldSpec.builder(type, entry.getKey(),
                    Modifier.PRIVATE, Modifier.FINAL).build();

            MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + StringUtils.capitalize(entry.getKey()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return this.$N", field)
                    .returns(type);

            entry.getValue().docs().ifPresent(
                    docs -> getterBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

            typeBuilder.addField(field)
                    .addMethod(getterBuilder.build());

            constructorBuilder.addParameter(type, entry.getKey())
                    .addStatement("this.$N = $N", field, entry.getKey());
        }

        typeBuilder.addMethod(constructorBuilder.build());

        return JavaFile.builder(typeDef.packageName().orElse(defaultPackage), typeBuilder.build())
                .indent("    ")
                .build();
    }

}

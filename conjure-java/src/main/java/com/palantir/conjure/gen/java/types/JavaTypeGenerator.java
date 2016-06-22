/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.Throwables;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.StringCleanup;
import com.palantir.conjure.gen.java.TypeMapper;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public final class JavaTypeGenerator {

    private final TypesDefinition types;
    private final TypeMapper typeMapper;

    public JavaTypeGenerator(TypesDefinition types, Settings settings) {
        this.types = types;
        this.typeMapper = new TypeMapper(types, settings.optionalTypeStrategy());
    }

    public TypeMapper getJavaTypeMapper() {
        return typeMapper;
    }

    public Set<JavaFile> generate() {
        return types.definitions().objects().entrySet().stream()
                .map(entry -> generateType(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public void emit(File outputDir) {
        generate().forEach(f -> {
            try {
                f.writeTo(outputDir);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });
    }

    private JavaFile generateType(String typeName, ObjectTypeDefinition typeDef) {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        typeDef.docs().ifPresent(docs -> typeBuilder.addJavadoc("$L", StringCleanup.withEndOfLine(docs)));

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());

            FieldSpec field = FieldSpec.builder(type, entry.getKey(),
                    Modifier.PRIVATE, Modifier.FINAL).build();

            MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder("get" + StringCleanup.ucfirst(entry.getKey()))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return this.$N", field)
                    .returns(type);

            entry.getValue().docs().ifPresent(
                    docs -> getterBuilder.addJavadoc("$L", StringCleanup.withEndOfLine(docs)));

            typeBuilder.addField(field)
                    .addMethod(getterBuilder.build());

            constructorBuilder.addParameter(type, entry.getKey())
                    .addStatement("this.$N = $N", field, entry.getKey());
        }

        typeBuilder.addMethod(constructorBuilder.build());

        return JavaFile.builder(typeDef.packageName().orElse(types.definitions().defaultPackage()), typeBuilder.build())
                .indent("    ")
                .build();
    }

}

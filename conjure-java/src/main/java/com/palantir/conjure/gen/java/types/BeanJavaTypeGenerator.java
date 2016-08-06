/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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

        String typePackage = typeDef.packageName().orElse(defaultPackage);
        ClassName thisClass = ClassName.get(typePackage, typeName);

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        if (settings.ignoreUnknownProperties()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
                    .addMember("ignoreUnknown", "$L", true)
                    .build());
        }

        if (typeDef.docs().isPresent()) {
            typeBuilder.addJavadoc("$L", StringUtils.appendIfMissing(typeDef.docs().get(), "\n"));
        }

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        Set<FieldSpec> fields = Sets.newLinkedHashSet();

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());
            String jsonName = entry.getKey();

            FieldSpec field = FieldSpec.builder(type, Fields.toSafeFieldName(jsonName),
                    Modifier.PRIVATE, Modifier.FINAL).build();
            fields.add(field);

            constructorBuilder.addParameter(createConstructorParam(type, jsonName, field.name));
            constructorBuilder.addStatement("this.$N = $N", field, field.name);

            typeBuilder.addMethod(createGetter(type, field, entry.getValue().docs()));
        }

        typeBuilder.addMethod(constructorBuilder.build());

        typeBuilder.addFields(fields);

        MethodSpec equalTo = createEqualTo(thisClass, fields);
        typeBuilder.addMethod(createEquals(thisClass, equalTo));
        typeBuilder.addMethod(equalTo);

        typeBuilder.addMethod(createHashCode(thisClass, fields));

        return JavaFile.builder(typePackage, typeBuilder.build())
                .indent("    ")
                .build();
    }

    private static ParameterSpec createConstructorParam(TypeName type, String fieldKey, String fieldName) {
        return ParameterSpec.builder(type, fieldName)
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", fieldKey)
                        .build())
                .build();
    }

    private static MethodSpec createGetter(TypeName returnType, FieldSpec field, Optional<String> docs) {
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(generateGetterName(field.name))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$N", field)
                .returns(returnType);

        if (docs.isPresent()) {
            getterBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs.get(), "\n"));
        }
        return getterBuilder.build();
    }

    private static MethodSpec createEquals(TypeName thisClass, MethodSpec equalToMethod) {
        ParameterSpec other = ParameterSpec.builder(TypeName.OBJECT, "other").build();
        return MethodSpec.methodBuilder("equals")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(other)
                .returns(TypeName.BOOLEAN)
                .addStatement("return this == $1N || ($1N instanceof $2T && $3N(($2T) $1N))",
                        other, thisClass, equalToMethod)
                .build();
    }

    private static MethodSpec createEqualTo(TypeName thisClass, Set<FieldSpec> fields) {
        String equalsExpression = Joiner.on(" && ")
                .join(indexStringInRange("$1T.equals(this.$%1$dN, other.$%dN)", 2, fields.size() + 2));

        return MethodSpec.methodBuilder("equalTo")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(thisClass, "other")
                .returns(TypeName.BOOLEAN)
                .addStatement("return " + equalsExpression, append(Objects.class, fields))
                .build();
    }

    private static MethodSpec createHashCode(TypeName thisClass, Set<FieldSpec> fields) {
        String hashParameters = Joiner.on(", ").join(indexStringInRange("$%dN", 2, fields.size() + 2));

        return MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return $1T.hash(" + hashParameters + ")", append(Objects.class, fields))
                .build();
    }

    private static Iterator<String> indexStringInRange(String format, int lower, int upper) {
        return IntStream.range(lower, upper).mapToObj(i -> String.format(format, i)).iterator();
    }

    private static Object[] append(Object left, Collection<?> right) {
        return Stream.concat(Stream.of(left), right.stream()).toArray();
    }

    private static String generateGetterName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class BeanGenerator implements TypeGenerator {

    private final Settings settings;

    public BeanGenerator(Settings settings) {
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

        List<FieldSpec> fields = Lists.newArrayListWithCapacity(typeDef.fields().size());
        List<MethodSpec> getters = Lists.newArrayListWithCapacity(typeDef.fields().size());

        for (Entry<String, FieldDefinition> entry : typeDef.fields().entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());
            String jsonName = entry.getKey();

            FieldSpec field = FieldSpec.builder(type, Fields.toSafeFieldName(jsonName),
                    Modifier.PRIVATE, Modifier.FINAL).build();
            fields.add(field);

            getters.add(createGetter(type, field, entry.getValue().docs()));
        }

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

        typeBuilder.addFields(fields);
        typeBuilder.addMethod(createConstructor(typeMapper, typeDef.fields()));
        typeBuilder.addMethods(getters);
        typeBuilder.addMethod(createEquals(thisClass));
        typeBuilder.addMethod(createEqualTo(thisClass, fields));
        typeBuilder.addMethod(createHashCode(fields));
        typeBuilder.addMethod(createValidateFields(fields));
        typeBuilder.addMethod(createAddFieldIfMissing(fields.size()));
        typeBuilder.addType(BeanBuilderGenerator.generate(typeMapper, defaultPackage, typeName, typeDef));

        return JavaFile.builder(typePackage, typeBuilder.build())
                .skipJavaLangImports(true)
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

    private static MethodSpec createConstructor(TypeMapper typeMapper, Map<String, FieldDefinition> fields) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        List<String> fieldNames = Lists.newArrayListWithCapacity(fields.size());

        CodeBlock.Builder body = CodeBlock.builder();

        for (Entry<String, FieldDefinition> entry : fields.entrySet()) {
            TypeName type = typeMapper.getClassName(entry.getValue().type());
            String fieldKey = entry.getKey();
            String fieldName = Fields.toSafeFieldName(fieldKey);
            fieldNames.add(fieldName);

            builder.addParameter(createConstructorParam(type, fieldKey, fieldName));

            if (entry.getValue().type() instanceof ListType) {
                body.addStatement("$1T __$2N = new $3T<>($2N.size())", type, fieldName, ArrayList.class);
                body.addStatement("__$1N.addAll($1N)", fieldName);
                body.addStatement("this.$1N = $2T.unmodifiableList(__$1N)", fieldName, Collections.class);
            } else if (entry.getValue().type() instanceof SetType) {
                body.addStatement("$1T __$2N = new $3T<>($2N.size())", type, fieldName, LinkedHashSet.class);
                body.addStatement("__$1N.addAll($1N)", fieldName);
                body.addStatement("this.$1N = $2T.unmodifiableSet(__$1N)", fieldName, Collections.class);
            } else if (entry.getValue().type() instanceof MapType) {
                body.addStatement("$1T __$2N = new $3T<>($2N.size())", type, fieldName, LinkedHashMap.class);
                body.addStatement("__$1N.putAll($1N)", fieldName);
                body.addStatement("this.$1N = $2T.unmodifiableMap(__$1N)", fieldName, Map.class);
            } else {
                body.addStatement("this.$1N = $1N", fieldName);
            }
        }

        builder.addStatement("$L", Expressions.localMethodCall("validateFields", fieldNames));
        builder.addCode(body.build());

        return builder.build();
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

    private static MethodSpec createEquals(TypeName thisClass) {
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

    private static MethodSpec createEqualTo(TypeName thisClass, Collection<FieldSpec> fields) {
        String equalsExpression = Joiner.on(" && ")
                .join(indexStringInRange("this.$%1$dN.equals(other.$%1$dN)", 1, fields.size() + 1));

        return MethodSpec.methodBuilder("equalTo")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(thisClass, "other")
                .returns(TypeName.BOOLEAN)
                .addStatement("return " + equalsExpression, fields.toArray())
                .build();
    }

    private static MethodSpec createHashCode(Collection<FieldSpec> fields) {
        return MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.INT)
                .addStatement("return $L", Expressions.staticMethodCall(Objects.class, "hash", fields))
                .build();
    }

    private static MethodSpec createValidateFields(Collection<FieldSpec> fields) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("validateFields")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC);

        builder.addStatement("$T missingFields = null", ParameterizedTypeName.get(List.class, String.class));
        for (FieldSpec spec : fields) {
            builder.addParameter(ParameterSpec.builder(spec.type, spec.name).build());
            builder.addStatement("missingFields = addFieldIfMissing(missingFields, $N, $S)", spec, spec.name);
        }

        builder
            .beginControlFlow("if (missingFields != null)")
            .addStatement("throw new $T(\"Some required fields have not been set: \" + missingFields)",
                    IllegalStateException.class)
            .endControlFlow();
        return builder.build();
    }

    private static MethodSpec createAddFieldIfMissing(int fieldCount) {
        ParameterizedTypeName listOfStringType = ParameterizedTypeName.get(List.class, String.class);
        ParameterSpec listParam = ParameterSpec.builder(listOfStringType, "prev").build();
        ParameterSpec fieldValueParam = ParameterSpec.builder(TypeName.OBJECT, "fieldValue").build();
        ParameterSpec fieldNameParam = ParameterSpec.builder(ClassName.get(String.class), "fieldName").build();

        return MethodSpec.methodBuilder("addFieldIfMissing")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(listOfStringType)
                .addParameter(listParam)
                .addParameter(fieldValueParam)
                .addParameter(fieldNameParam)
                .addStatement("$T missingFields = $N", listOfStringType, listParam)
                .beginControlFlow("if ($N == null)", fieldValueParam)
                    .beginControlFlow("if (missingFields == null)")
                        .addStatement("missingFields = new $T<>($L)", ArrayList.class, fieldCount)
                    .endControlFlow()
                    .addStatement("missingFields.add($N)", fieldNameParam)
                .endControlFlow()
                .addStatement("return missingFields")
                .build();
    }

    private static Iterator<String> indexStringInRange(String format, int lower, int upper) {
        return IntStream.range(lower, upper).mapToObj(i -> String.format(format, i)).iterator();
    }

    private static String generateGetterName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
    }

}

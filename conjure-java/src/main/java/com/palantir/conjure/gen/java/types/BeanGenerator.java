/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.gen.java.ConjureAnnotations;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

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
            BaseObjectTypeDefinition typeDef) {
        if (typeDef instanceof ObjectTypeDefinition) {
            return generateBeanType(types, defaultPackage, typeName, (ObjectTypeDefinition) typeDef, settings);
        } else if (typeDef instanceof EnumTypeDefinition) {
            return EnumGenerator.generateEnumType(
                    types, defaultPackage, typeName, (EnumTypeDefinition) typeDef, settings);
        } else if (typeDef instanceof AliasTypeDefinition) {
            return AliasGenerator.generateAliasType(
                    types, defaultPackage, typeName, (AliasTypeDefinition) typeDef, settings);
        }
        throw new IllegalArgumentException("Unknown object definition type " + typeDef.getClass());
    }

    private static JavaFile generateBeanType(
            TypesDefinition types,
            String defaultPackage,
            String typeName,
            ObjectTypeDefinition typeDef,
            Settings settings) {
        TypeMapper typeMapper = new TypeMapper(types, settings.optionalTypeStrategy());

        String typePackage = typeDef.packageName().orElse(defaultPackage);
        ClassName objectClass = ClassName.get(typePackage, typeName);
        ClassName builderClass = ClassName.get(objectClass.packageName(), objectClass.simpleName(), "Builder");

        Collection<EnrichedField> fields = createFields(typeMapper, typeDef.fields());
        Collection<FieldSpec> poetFields = EnrichedField.toPoetSpecs(fields);
        Collection<FieldSpec> nonPrimitivePoetFields = Collections2.filter(poetFields, f -> !f.type.isPrimitive());

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(poetFields)
                .addMethod(createConstructor(fields, nonPrimitivePoetFields))
                .addMethods(createGetters(fields))
                .addMethod(createEquals(objectClass))
                .addMethod(createEqualTo(objectClass, poetFields))
                .addMethod(createHashCode(poetFields))
                .addMethod(createToString(typeName, poetFields));

        if (!nonPrimitivePoetFields.isEmpty()) {
            typeBuilder
                    .addMethod(createValidateFields(nonPrimitivePoetFields))
                    .addMethod(createAddFieldIfMissing(nonPrimitivePoetFields.size()));
        }

        typeBuilder
                .addMethod(createBuilder(builderClass))
                .addType(BeanBuilderGenerator.generate(typeMapper, defaultPackage, objectClass, builderClass, typeDef));

        if (settings.ignoreUnknownProperties()) {
            typeBuilder.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
                    .addMember("ignoreUnknown", "$L", true)
                    .build());
        }

        typeBuilder.addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(BeanGenerator.class));

        if (typeDef.docs().isPresent()) {
            typeBuilder.addJavadoc("$L", StringUtils.appendIfMissing(typeDef.docs().get(), "\n"));
        }

        return JavaFile.builder(typePackage, typeBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static Collection<EnrichedField> createFields(TypeMapper typeMapper, Map<String, FieldDefinition> fields) {
        return fields.entrySet().stream()
                .map(e -> EnrichedField.of(e.getKey(), e.getValue(), FieldSpec.builder(
                            typeMapper.getClassName(e.getValue().type()),
                            Fields.toSafeFieldName(e.getKey()),
                            Modifier.PRIVATE, Modifier.FINAL)
                        .build()))
                .collect(Collectors.toList());
    }

    private static MethodSpec createConstructor(
            Collection<EnrichedField> fields,
            Collection<FieldSpec> nonPrimitivePoetFields) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);

        if (!nonPrimitivePoetFields.isEmpty()) {
            builder.addStatement("$L", Expressions.localMethodCall("validateFields", nonPrimitivePoetFields));
        }

        CodeBlock.Builder body = CodeBlock.builder();
        for (EnrichedField field : fields) {
            FieldSpec spec = field.poetSpec();

            builder.addParameter(ParameterSpec.builder(spec.type, spec.name)
                    .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", field.jsonKey())
                        .build())
                    .build());

            if (field.conjureDef().type() instanceof ListType) {
                // TODO contribute a fix to JavaPoet that parses $T correctly for a JavaPoet FieldSpec
                body.addStatement("this.$1N = $2T.unmodifiableList(new $3T<>($1N))",
                        spec, Collections.class, ArrayList.class);
            } else if (field.conjureDef().type() instanceof SetType) {
                body.addStatement("this.$1N = $2T.unmodifiableSet(new $3T<>($1N))",
                        spec, Collections.class, LinkedHashSet.class);
            } else if (field.conjureDef().type() instanceof MapType) {
                body.addStatement("this.$1N = $2T.unmodifiableMap(new $3T<>($1N))",
                        spec, Collections.class, LinkedHashMap.class);
            } else {
                body.addStatement("this.$1N = $1N", spec);
            }
        }

        builder.addCode(body.build());

        return builder.build();
    }

    private static Collection<MethodSpec> createGetters(Collection<EnrichedField> fields) {
        return fields.stream()
                .map(f -> createGetter(f.poetSpec(), f.conjureDef().docs()))
                .collect(Collectors.toList());
    }

    private static MethodSpec createGetter(FieldSpec field, Optional<String> docs) {
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(generateGetterName(field.name))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return this.$N", field.name)
                .returns(field.type);

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
        List<String> comparisons = new ArrayList<>(fields.size());
        int fieldNumber = 1;
        for (FieldSpec spec : fields) {
            if (spec.type.isPrimitive()) {
                comparisons.add(String.format("this.$%1$dN == other.$%1$dN", fieldNumber));
            } else {
                comparisons.add(String.format("this.$%1$dN.equals(other.$%1$dN)", fieldNumber));
            }
            fieldNumber = fieldNumber + 1;
        }
        String equalsExpression = Joiner.on(" && ").join(comparisons);

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

    private static MethodSpec createToString(String thisClassName, Collection<FieldSpec> poetFields) {
        CodeBlock returnStatement = CodeBlock.builder()
                .add("return new $T($S).append(\"{\")\n", StringBuilder.class, thisClassName)
                .indent()
                    .indent()
                        .add(CodeBlocks.of(poetFields.stream()
                            .map(f -> CodeBlock.of(".append($1S).append(\": \").append($1N)\n", f.name))
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

    private static MethodSpec createBuilder(ClassName builderClass) {
        return MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(builderClass)
                .addStatement("return new $T()", builderClass)
                .build();
    }

    public static String generateGetterName(String fieldName) {
        return "get" + StringUtils.capitalize(fieldName);
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

    @Value.Immutable
    interface EnrichedField {
        @Value.Parameter
        String jsonKey();

        @Value.Parameter
        FieldDefinition conjureDef();

        @Value.Parameter
        FieldSpec poetSpec();

        static EnrichedField of(String jsonKey, FieldDefinition conjureDef, FieldSpec poetSpec) {
            return ImmutableEnrichedField.of(jsonKey, conjureDef, poetSpec);
        }

        static Collection<FieldSpec> toPoetSpecs(Collection<EnrichedField> fields) {
            return fields.stream().map(EnrichedField::poetSpec).collect(Collectors.toList());
        }
    }

}

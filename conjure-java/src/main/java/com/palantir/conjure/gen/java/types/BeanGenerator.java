/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Collections2;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.ObjectDefinitions;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjurePackage;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.FieldName;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.defs.types.TypeName;
import com.palantir.conjure.defs.types.UnionTypeDefinition;
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
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

public final class BeanGenerator implements TypeGenerator {

    private final Settings settings;

    /** The maximum number of parameters for which a static factory method is generated in addition to the builder. */
    private static final int MAX_NUM_PARAMS_FOR_FACTORY = 3;

    public BeanGenerator(Settings settings) {
        this.settings = settings;
    }

    @Override
    public JavaFile generateType(
            TypesDefinition types,
            ConjureImports importedTypes,
            Optional<ConjurePackage> defaultPackage,
            com.palantir.conjure.defs.types.TypeName typeName,
            BaseObjectTypeDefinition typeDef) {
        TypeMapper typeMapper = new TypeMapper(types, importedTypes);
        if (typeDef instanceof ObjectTypeDefinition) {
            return generateBeanType(typeMapper, defaultPackage, typeName, (ObjectTypeDefinition) typeDef);
        } else if (typeDef instanceof UnionTypeDefinition) {
            return UnionGenerator.generateUnionType(
                    typeMapper, defaultPackage, typeName, (UnionTypeDefinition) typeDef);
        } else if (typeDef instanceof EnumTypeDefinition) {
            return EnumGenerator.generateEnumType(
                    defaultPackage, typeName, (EnumTypeDefinition) typeDef, settings.supportUnknownEnumValues());
        } else if (typeDef instanceof AliasTypeDefinition) {
            return AliasGenerator.generateAliasType(
                    typeMapper, defaultPackage, typeName, (AliasTypeDefinition) typeDef);
        }
        throw new IllegalArgumentException("Unknown object definition type " + typeDef.getClass());
    }

    private JavaFile generateBeanType(
            TypeMapper typeMapper,
            Optional<ConjurePackage> defaultPackage,
            TypeName typeName,
            ObjectTypeDefinition typeDef) {

        ConjurePackage typePackage = ObjectDefinitions.getPackage(typeDef.conjurePackage(), defaultPackage, typeName);
        ClassName objectClass = ClassName.get(typePackage.name(), typeName.name());
        ClassName builderClass = ClassName.get(objectClass.packageName(), objectClass.simpleName(), "Builder");

        Collection<EnrichedField> fields = createFields(typeMapper, typeDef.fields());
        Collection<FieldSpec> poetFields = EnrichedField.toPoetSpecs(fields);
        Collection<FieldSpec> nonPrimitivePoetFields = Collections2.filter(poetFields, f -> !f.type.isPrimitive());

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(typeName.name())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(JsonDeserialize.class)
                        .addMember("builder", "$T.class", builderClass).build())
                .addFields(poetFields)
                .addMethod(createConstructor(fields, nonPrimitivePoetFields))
                .addMethods(createGetters(fields))
                .addMethod(MethodSpecs.createEquals(objectClass))
                .addMethod(MethodSpecs.createEqualTo(objectClass, poetFields))
                .addMethod(MethodSpecs.createHashCode(poetFields))
                .addMethod(MethodSpecs.createToString(typeName.name(), poetFields));

        if (poetFields.size() <= MAX_NUM_PARAMS_FOR_FACTORY) {
            typeBuilder.addMethod(createStaticFactoryMethod(poetFields, objectClass));
        }

        if (!nonPrimitivePoetFields.isEmpty()) {
            typeBuilder
                    .addMethod(createValidateFields(nonPrimitivePoetFields))
                    .addMethod(createAddFieldIfMissing(nonPrimitivePoetFields.size()));
        }

        typeBuilder
                .addMethod(createBuilder(builderClass))
                .addType(BeanBuilderGenerator.generate(
                        typeMapper, objectClass, builderClass, typeDef, settings.ignoreUnknownProperties()));

        typeBuilder.addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(BeanGenerator.class));

        typeDef.docs().ifPresent(docs -> typeBuilder.addJavadoc("$L", StringUtils.appendIfMissing(docs, "\n")));

        return JavaFile.builder(typePackage.name(), typeBuilder.build())
                .skipJavaLangImports(true)
                .indent("    ")
                .build();
    }

    private static Collection<EnrichedField> createFields(
            TypeMapper typeMapper, Map<FieldName, FieldDefinition> fields) {
        return fields.entrySet().stream()
                .map(e -> EnrichedField.of(e.getKey().name(), e.getValue(), FieldSpec.builder(
                        typeMapper.getClassName(e.getValue().type()),
                        e.getKey().toCase(FieldName.Case.LOWER_CAMEL_CASE).name(),
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

            builder.addParameter(spec.type, spec.name);

            if (field.conjureDef().type() instanceof ListType) {
                // TODO(melliot): contribute a fix to JavaPoet that parses $T correctly for a JavaPoet FieldSpec
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
                .map(f -> createGetter(f))
                .collect(Collectors.toList());
    }

    private static MethodSpec createGetter(EnrichedField field) {
        MethodSpec.Builder getterBuilder = MethodSpec.methodBuilder(generateGetterName(field.poetSpec().name))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", field.jsonKey())
                        .build())
                .addStatement("return this.$N", field.poetSpec().name)
                .returns(field.poetSpec().type);

        if (field.conjureDef().docs().isPresent()) {
            getterBuilder.addJavadoc("$L", StringUtils.appendIfMissing(field.conjureDef().docs().get(), "\n"));
        }
        return getterBuilder.build();
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

    private static MethodSpec createStaticFactoryMethod(Collection<FieldSpec> fields, ClassName objectClass) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("of")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(objectClass);

        builder.addCode("return builder()");
        for (FieldSpec spec : fields) {
            if (isOptional(spec)) {
                builder.addCode("\n    .$L(Optional.of($L))", spec.name, spec.name);
            } else {
                builder.addCode("\n    .$L($L)", spec.name, spec.name);
            }
            builder.addParameter(ParameterSpec.builder(getTypeNameWithoutOptional(spec), spec.name).build());
        }
        builder.addCode("\n    .build();\n");
        return builder.build();
    }

    private static MethodSpec createAddFieldIfMissing(int fieldCount) {
        ParameterizedTypeName listOfStringType = ParameterizedTypeName.get(List.class, String.class);
        ParameterSpec listParam = ParameterSpec.builder(listOfStringType, "prev").build();
        ParameterSpec fieldValueParam =
                ParameterSpec.builder(com.squareup.javapoet.TypeName.OBJECT, "fieldValue").build();
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

    private static com.squareup.javapoet.TypeName getTypeNameWithoutOptional(FieldSpec spec) {
        if (!isOptional(spec)) {
            return spec.type;
        }
        return ((ParameterizedTypeName) spec.type).typeArguments.get(0);
    }

    private static boolean isOptional(FieldSpec spec) {
        if (!(spec.type instanceof ParameterizedTypeName)) {
            // spec isn't a wrapper class
            return false;
        }
        return ((ParameterizedTypeName) spec.type).rawType.simpleName().equals("Optional");
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

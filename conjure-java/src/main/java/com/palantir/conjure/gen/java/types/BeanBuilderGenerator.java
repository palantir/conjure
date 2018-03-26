/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.types.TypeVisitor;
import com.palantir.conjure.gen.java.ConjureAnnotations;
import com.palantir.conjure.gen.java.types.BeanGenerator.EnrichedField;
import com.palantir.conjure.gen.java.util.JavaNameSanitizer;
import com.palantir.conjure.lib.internal.ConjureCollections;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.Type;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;
import org.apache.commons.lang3.StringUtils;

public final class BeanBuilderGenerator {
    private final TypeMapper typeMapper;
    private final ClassName builderClass;
    private final ClassName objectClass;

    private BeanBuilderGenerator(TypeMapper typeMapper, ClassName builderClass, ClassName objectClass) {
        this.typeMapper = typeMapper;
        this.builderClass = builderClass;
        this.objectClass = objectClass;
    }

    public static TypeSpec generate(
            TypeMapper typeMapper,
            ClassName objectClass,
            ClassName builderClass,
            ObjectDefinition typeDef,
            boolean ignoreUnknownProperties) {

        return new BeanBuilderGenerator(typeMapper, builderClass, objectClass)
                .generate(typeDef, ignoreUnknownProperties);
    }

    private TypeSpec generate(ObjectDefinition typeDef, boolean ignoreUnknownProperties) {

        Collection<EnrichedField> enrichedFields = enrichFields(typeDef.getFields());
        Collection<FieldSpec> poetFields = EnrichedField.toPoetSpecs(enrichedFields);

        TypeSpec.Builder builder = TypeSpec.classBuilder("Builder")
                .addAnnotation(ConjureAnnotations.getConjureGeneratedAnnotation(BeanBuilderGenerator.class))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(poetFields)
                .addMethod(createConstructor())
                .addMethod(createFromObject(enrichedFields))
                .addMethods(createSetters(enrichedFields))
                .addMethod(createBuild(poetFields));

        if (ignoreUnknownProperties) {
            builder.addAnnotation(AnnotationSpec.builder(JsonIgnoreProperties.class)
                    .addMember("ignoreUnknown", "$L", true)
                    .build());
        }

        return builder.build();
    }

    private Collection<EnrichedField> enrichFields(List<FieldDefinition> fields) {
        return fields.stream()
                .map(e -> createField(e.getFieldName(), e))
                .collect(Collectors.toList());
    }

    private static MethodSpec createConstructor() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
    }

    private MethodSpec createFromObject(Collection<EnrichedField> enrichedFields) {
        CodeBlock assignmentBlock = CodeBlocks.of(Iterables.transform(enrichedFields,
                enrichedField -> CodeBlocks.statement(
                        "$1N(other.$2N())",
                        enrichedField.poetSpec().name,
                        enrichedField.getterName())));

        return MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass)
                .addParameter(objectClass, "other")
                .addCode(assignmentBlock)
                .addStatement("return this")
                .build();
    }

    private EnrichedField createField(FieldName fieldName, FieldDefinition field) {
        FieldSpec.Builder spec = FieldSpec.builder(
                typeMapper.getClassName(field.getType()),
                JavaNameSanitizer.sanitize(fieldName),
                Modifier.PRIVATE);

        if (field.getType().accept(TypeVisitor.IS_LIST)) {
            spec.initializer("new $T<>()", ArrayList.class);
        } else if (field.getType().accept(TypeVisitor.IS_SET)) {
            spec.initializer("new $T<>()", LinkedHashSet.class);
        } else if (field.getType().accept(TypeVisitor.IS_MAP)) {
            spec.initializer("new $T<>()", LinkedHashMap.class);
        } else if (field.getType().accept(TypeVisitor.IS_OPTIONAL)) {
            spec.initializer("$T.empty()", asRawType(typeMapper.getClassName(field.getType())));
        }
        // else no initializer

        return EnrichedField.of(fieldName, field, spec.build());
    }

    private Iterable<MethodSpec> createSetters(Collection<EnrichedField> fields) {
        Collection<MethodSpec> setters = Lists.newArrayListWithExpectedSize(fields.size());
        for (EnrichedField field : fields) {
            setters.add(createSetter(field));
            setters.addAll(createAuxiliarySetters(field));
        }
        return setters;
    }

    private MethodSpec createSetter(EnrichedField enriched) {
        FieldSpec field = enriched.poetSpec();
        Type type = enriched.conjureDef().getType();
        AnnotationSpec jsonSetterAnnotation = AnnotationSpec.builder(JsonSetter.class)
                .addMember("value", "$S", enriched.fieldName().get())
                .build();
        boolean shouldClearFirst = true;
        return publicSetter(enriched)
                .addParameter(widenToIterableIfPossible(field.type, type), field.name)
                .addCode(typeAwareAssignment(enriched, type, shouldClearFirst))
                .addStatement("return this")
                .addAnnotation(jsonSetterAnnotation)
                .build();
    }

    private MethodSpec createCollectionSetter(String prefix, EnrichedField enriched) {
        FieldSpec field = enriched.poetSpec();
        Type type = enriched.conjureDef().getType();
        boolean shouldClearFirst = false;
        return MethodSpec.methodBuilder(prefix + StringUtils.capitalize(field.name))
                .addJavadoc(enriched.conjureDef().getDocs().map(Documentation::get).orElse(""))
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass)
                .addParameter(widenToIterableIfPossible(field.type, type), field.name)
                .addCode(typeAwareAssignment(enriched, type, shouldClearFirst))
                .addStatement("return this")
                .build();
    }

    private TypeName widenToIterableIfPossible(TypeName current, Type type) {
        if (type.accept(TypeVisitor.IS_LIST)) {
            TypeName typeName = typeMapper.getClassName(type.accept(TypeVisitor.LIST).getItemType()).box();
            return ParameterizedTypeName.get(ClassName.get(Iterable.class), typeName);
        }

        if (type.accept(TypeVisitor.IS_SET)) {
            TypeName typeName = typeMapper.getClassName(type.accept(TypeVisitor.SET).getItemType()).box();
            return ParameterizedTypeName.get(ClassName.get(Iterable.class), typeName);
        }

        return current;
    }

    private static CodeBlock typeAwareAssignment(EnrichedField enriched, Type type, boolean shouldClearFirst) {
        FieldSpec spec = enriched.poetSpec();
        if (type.accept(TypeVisitor.IS_LIST) || type.accept(TypeVisitor.IS_SET)) {
            CodeBlock addStatement = CodeBlocks.statement(
                    "$1T.addAll(this.$2N, $3L)",
                    ConjureCollections.class,
                    spec.name,
                    Expressions.requireNonNull(spec.name, enriched.fieldName().get() + " cannot be null"));
            return shouldClearFirst ? CodeBlocks.of(CodeBlocks.statement("this.$1N.clear()", spec.name), addStatement)
                    : addStatement;
        } else if (type.accept(TypeVisitor.IS_MAP)) {
            CodeBlock addStatement = CodeBlocks.statement(
                    "this.$1N.putAll($2L)", spec.name,
                    Expressions.requireNonNull(spec.name, enriched.fieldName().get() + " cannot be null"));
            return shouldClearFirst ? CodeBlocks.of(CodeBlocks.statement("this.$1N.clear()", spec.name), addStatement)
                    : addStatement;
        } else if (type.accept(TypeVisitor.IS_BINARY)) {
            return CodeBlock.builder()
                    .addStatement("$L", Expressions.requireNonNull(
                            spec.name, enriched.fieldName().get() + " cannot be null"))
                    .addStatement("this.$1N = $2T.allocate($1N.remaining()).put($1N.duplicate())",
                            spec.name,
                            ByteBuffer.class)
                    .addStatement("this.$1N.rewind()", spec.name)
                    .build();
        } else {
            CodeBlock nullCheckedValue = spec.type.isPrimitive()
                    ? CodeBlock.of("$N", spec.name) // primitive types can't be null, so no need for requireNonNull!
                    : Expressions.requireNonNull(spec.name, enriched.fieldName().get() + " cannot be null");
            return CodeBlocks.statement("this.$1L = $2L", spec.name, nullCheckedValue);
        }
    }

    private List<MethodSpec> createAuxiliarySetters(EnrichedField enriched) {
        Type type = enriched.conjureDef().getType();

        if (type.accept(TypeVisitor.IS_LIST)) {
            return ImmutableList.of(
                    createCollectionSetter("addAll", enriched),
                    createItemSetter(enriched, type.accept(TypeVisitor.LIST).getItemType()));
        }

        if (type.accept(TypeVisitor.IS_SET)) {
            return ImmutableList.of(
                    createCollectionSetter("addAll", enriched),
                    createItemSetter(enriched, type.accept(TypeVisitor.SET).getItemType()));
        }

        if (type.accept(TypeVisitor.IS_MAP)) {
            return ImmutableList.of(
                    createCollectionSetter("putAll", enriched),
                    createMapSetter(enriched));
        }

        if (type.accept(TypeVisitor.IS_OPTIONAL)) {
            return ImmutableList.of(
                    createOptionalSetter(enriched));
        }

        return ImmutableList.of();
    }

    private MethodSpec createOptionalSetter(EnrichedField enriched) {
        FieldSpec field = enriched.poetSpec();
        OptionalType type = enriched.conjureDef().getType().accept(TypeVisitor.OPTIONAL);
        return publicSetter(enriched)
                .addParameter(typeMapper.getClassName(type.getItemType()), field.name)
                .addCode(optionalAssignmentStatement(enriched, type))
                .addStatement("return this")
                .build();
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private CodeBlock optionalAssignmentStatement(EnrichedField enriched, OptionalType type) {
        FieldSpec spec = enriched.poetSpec();
        if (type.getItemType().accept(TypeVisitor.IS_PRIMITIVE)) {

            switch (type.getItemType().accept(TypeVisitor.PRIMITIVE).get()) {
                case INTEGER:
                case DOUBLE:
                case BOOLEAN:
                    return CodeBlocks.statement("this.$1N = $2T.of($1N)",
                            enriched.poetSpec().name, asRawType(typeMapper.getClassName(Type.optional(type))));
                case SAFELONG:
                case STRING:
                case RID:
                case BEARERTOKEN:
                case UUID:
                case ANY:
                case DATETIME:
                case BINARY:
                default:
                    // not special
            }
        }
        return CodeBlocks.statement("this.$1N = $2T.of($3L)",
                spec.name, Optional.class, Expressions.requireNonNull(
                        spec.name, enriched.fieldName().get() + " cannot be null"));
    }

    private MethodSpec createItemSetter(EnrichedField enriched, Type itemType) {
        FieldSpec field = enriched.poetSpec();
        return publicSetter(enriched)
                .addParameter(typeMapper.getClassName(itemType), field.name)
                .addStatement("this.$1N.add($1N)", field.name)
                .addStatement("return this")
                .build();
    }

    private MethodSpec createMapSetter(EnrichedField enriched) {
        MapType type = enriched.conjureDef().getType().accept(TypeVisitor.MAP);
        return publicSetter(enriched)
                .addParameter(typeMapper.getClassName(type.getKeyType()), "key")
                .addParameter(typeMapper.getClassName(type.getValueType()), "value")
                .addStatement("this.$1N.put(key, value)", enriched.poetSpec().name)
                .addStatement("return this")
                .build();
    }

    private MethodSpec.Builder publicSetter(EnrichedField enriched) {
        return MethodSpec.methodBuilder(enriched.poetSpec().name)
                .addJavadoc(enriched.conjureDef().getDocs().map(Documentation::get).orElse(""))
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClass);
    }

    private MethodSpec createBuild(Collection<FieldSpec> fields) {
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(objectClass)
                .addStatement("return new $L", Expressions.constructorCall(objectClass, fields))
                .build();
    }

    private static TypeName asRawType(TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) type).rawType;
        }
        return type;
    }
}

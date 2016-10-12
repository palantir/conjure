/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.gen.java.types.BeanGenerator.EnrichedField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

public final class BeanBuilderGenerator {

    private BeanBuilderGenerator() {}

    public static TypeSpec generate(
            TypeMapper typeMapper,
            String defaultPackage,
            ClassName objectClass,
            ClassName builderClass,
            ObjectTypeDefinition typeDef) {
        Collection<EnrichedField> fields = createFields(typeMapper, typeDef.fields());
        Collection<FieldSpec> poetFields = EnrichedField.toPoetSpecs(fields);

        return TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addFields(poetFields)
                .addMethod(createConstructor())
                .addMethod(createFromObject(builderClass, objectClass, fields))
                .addMethods(createSetters(builderClass, typeMapper, fields))
                .addMethod(createBuild(objectClass, poetFields))
                .build();
    }

    private static MethodSpec createConstructor() {
        return MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
    }

    private static MethodSpec createFromObject(
            ClassName builderClass,
            ClassName objectClass,
            Collection<EnrichedField> fields) {
        return MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(objectClass, "other")
                .returns(builderClass)
                .addCode(CodeBlocks.of(Iterables.transform(fields,
                        f -> CodeBlocks.statement("$1N(other.$2N())",
                                f.poetSpec().name, BeanGenerator.generateGetterName(f.poetSpec().name)))))
                .addStatement("return this")
                .build();
    }

    private static Collection<EnrichedField> createFields(TypeMapper typeMapper, Map<String, FieldDefinition> fields) {
        return fields.entrySet().stream()
                .map(e -> createField(typeMapper, e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static EnrichedField createField(TypeMapper typeMapper, String jsonKey, FieldDefinition field) {
        FieldSpec.Builder spec = FieldSpec.builder(
                typeMapper.getClassName(field.type()),
                Fields.toSafeFieldName(jsonKey),
                Modifier.PRIVATE);

        if (field.type() instanceof ListType) {
            spec.initializer("new $T<>()", ArrayList.class);
        } else if (field.type() instanceof SetType) {
            spec.initializer("new $T<>()", LinkedHashSet.class);
        } else if (field.type() instanceof MapType) {
            spec.initializer("new $T<>()", LinkedHashMap.class);
        } else if (field.type() instanceof OptionalType) {
            spec.initializer("$T.$L()",
                    asRawType(typeMapper.getClassName(field.type())),
                    typeMapper.getAbsentMethodName());
        }
        // else no initializer

        return EnrichedField.of(jsonKey, field, spec.build());
    }

    private static Iterable<MethodSpec> createSetters(
            ClassName builderClass,
            TypeMapper typeMapper,
            Collection<EnrichedField> fields) {
        Collection<MethodSpec> setters = Lists.newArrayListWithExpectedSize(fields.size());
        for (EnrichedField field : fields) {
            setters.add(createSetter(builderClass, field.poetSpec(), field.conjureDef().type(), typeMapper));
            createAuxiliarySetter(builderClass, typeMapper, field.poetSpec(), field.conjureDef().type())
                .ifPresent(setters::add);
        }
        return setters;
    }

    private static MethodSpec createSetter(
            ClassName builderClass,
            FieldSpec field,
            ConjureType type,
            TypeMapper typeMapper) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(widenToCollectionIfPossible(field.type, type, typeMapper), field.name)
                .returns(builderClass)
                .addCode(typeAwareSet(field, type))
                .addStatement("return this").build();
    }

    private static TypeName widenToCollectionIfPossible(TypeName current, ConjureType type, TypeMapper typeMapper) {
        if (type instanceof ListType) {
            return ParameterizedTypeName.get(ClassName.get(Collection.class),
                    typeMapper.getClassName(((ListType) type).itemType()));
        } else if (type instanceof SetType) {
            return ParameterizedTypeName.get(ClassName.get(Collection.class),
                    typeMapper.getClassName(((SetType) type).itemType()));
        }
        return current;
    }

    private static CodeBlock typeAwareSet(FieldSpec spec, ConjureType type) {
        if (type instanceof ListType || type instanceof SetType) {
            return CodeBlocks.statement("this.$1N.addAll($2T.requireNonNull($1N, \"$1N cannot be null\"))",
                    spec.name, Objects.class);
        } else if (type instanceof MapType) {
            return CodeBlocks.statement("this.$1N.putAll($2T.requireNonNull($1N, \"$1N cannot be null\"))",
                    spec.name, Objects.class);
        } else if (spec.type.isPrimitive()) {
            // primitive type non-nullity already enforced
            return CodeBlocks.statement("this.$1N = $1N", spec.name);
        } else {
            return CodeBlocks.statement("this.$1N = $2T.requireNonNull($1N, \"$1N cannot be null\")",
                    spec.name, Objects.class);
        }
    }

    private static Optional<MethodSpec> createAuxiliarySetter(
            ClassName builderClass,
            TypeMapper typeMapper,
            FieldSpec field,
            ConjureType type) {
        if (type instanceof ListType) {
            return Optional.of(createItemSetter(builderClass, typeMapper, field, ((ListType) type).itemType()));
        } else if (type instanceof SetType) {
            return Optional.of(createItemSetter(builderClass, typeMapper, field, ((SetType) type).itemType()));
        } else if (type instanceof MapType) {
            return Optional.of(createMapSetter(builderClass, typeMapper, field, (MapType) type));
        } else if (type instanceof OptionalType) {
            return Optional.of(createOptionalSetter(builderClass, typeMapper, field, (OptionalType) type));
        }
        return Optional.empty();
    }

    private static MethodSpec createOptionalSetter(
            ClassName builderClass,
            TypeMapper typeMapper,
            FieldSpec field,
            OptionalType type) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeMapper.getClassName(type.itemType()), field.name)
                .returns(builderClass)
                .addCode(optionalAssignmentStatement(typeMapper, field, type))
                .addStatement("return this")
                .build();
    }

    private static CodeBlock optionalAssignmentStatement(TypeMapper typeMapper, FieldSpec field, OptionalType type) {
        if (type.itemType() instanceof PrimitiveType) {
            switch ((PrimitiveType) type.itemType()) {
                case INTEGER:
                case DOUBLE:
                case BOOLEAN:
                    return CodeBlocks.statement("this.$1N = $2T.of($1N)",
                            field.name, asRawType(typeMapper.getClassName(type)));
                case STRING:
                default:
                    // not special
            }
        }
        return CodeBlocks.statement("this.$1N = $2T.of($3T.requireNonNull($1N, \"$1N cannot be null\"))",
                field.name, typeMapper.getOptionalType(), Objects.class);
    }

    private static MethodSpec createItemSetter(
            ClassName builderClass,
            TypeMapper typeMapper,
            FieldSpec field,
            ConjureType type) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeMapper.getClassName(type), field.name)
                .returns(builderClass)
                .addStatement("this.$1N.add($1N)", field.name)
                .addStatement("return this")
                .build();
    }

    private static MethodSpec createMapSetter(
            ClassName builderClass,
            TypeMapper typeMapper,
            FieldSpec field,
            MapType type) {
        return MethodSpec.methodBuilder(field.name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeMapper.getClassName(type.keyType()), "key")
                .addParameter(typeMapper.getClassName(type.valueType()), "value")
                .returns(builderClass)
                .addStatement("this.$1N.put(key, value)", field.name)
                .addStatement("return this")
                .build();
    }

    private static MethodSpec createBuild(ClassName objectClass, Collection<FieldSpec> fields) {
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

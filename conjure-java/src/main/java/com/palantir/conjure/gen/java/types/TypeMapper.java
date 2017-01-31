/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SetType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class TypeMapper {

    public enum OptionalTypeStrategy {
        JAVA8(java.util.Optional.class, "empty"),
        GUAVA(com.google.common.base.Optional.class, "absent");

        private ClassName clazz;
        private String absentMethodName;

        OptionalTypeStrategy(Class<?> clazz, String absentMethodName) {
            this.clazz = ClassName.get(clazz);
            this.absentMethodName = absentMethodName;
        }

        public ClassName getClassName() {
            return clazz;
        }

        public String getAbsentMethodName() {
            return absentMethodName;
        }
    }

    private final TypesDefinition types;
    private final OptionalTypeStrategy optionalTypeStrategy;

    public TypeMapper(TypesDefinition types, OptionalTypeStrategy optionalTypeStrategy) {
        this.types = types;
        this.optionalTypeStrategy = optionalTypeStrategy;
    }

    public TypeName getClassName(ConjureType type) {
        if (type instanceof OptionalType) {
            return getClassNameForOptionalType((OptionalType) type);
        } else if (type instanceof SetType) {
            return getClassNameForSetType((SetType) type);
        } else if (type instanceof ListType) {
            return getClassNameForListType((ListType) type);
        } else if (type instanceof MapType) {
            return getClassNameForMapType((MapType) type);
        } else if (type instanceof PrimitiveType) {
            return primitiveTypeToClassName((PrimitiveType) type);
        } else if (type instanceof ReferenceType) {
            return referenceTypeToClassName((ReferenceType) type);
        } else if (type instanceof AnyType) {
            return ClassName.get(Object.class);
        }
        throw new IllegalStateException("Unexpected type " + type.getClass());
    }

    public ClassName getOptionalType() {
        return optionalTypeStrategy.getClassName();
    }

    public String getAbsentMethodName() {
        return optionalTypeStrategy.getAbsentMethodName();
    }

    private TypeName getClassNameForMapType(MapType type) {
        return ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                boxIfPrimitive(getClassName(type.keyType())),
                boxIfPrimitive(getClassName(type.valueType())));
    }

    private TypeName getClassNameForListType(ListType type) {
        TypeName itemType = boxIfPrimitive(getClassName(type.itemType()));
        return ParameterizedTypeName.get(ClassName.get(java.util.List.class), itemType);
    }

    private TypeName getClassNameForSetType(SetType type) {
        TypeName itemType = boxIfPrimitive(getClassName(type.itemType()));
        return ParameterizedTypeName.get(ClassName.get(java.util.Set.class), itemType);
    }

    private TypeName getClassNameForOptionalType(OptionalType type) {
        if (type.itemType() instanceof PrimitiveType && optionalTypeStrategy == OptionalTypeStrategy.JAVA8) {
            // special handling for primitive optionals with Java 8
            switch ((PrimitiveType) type.itemType()) {
                case DOUBLE:
                    return ClassName.get(OptionalDouble.class);
                case INTEGER:
                    return ClassName.get(OptionalInt.class);
                case BOOLEAN:
                    // no OptionalBoolean type
                case STRING:
                default:
                    // treat normally
            }
        }
        TypeName itemType = getClassName(type.itemType());
        if (itemType.isPrimitive()) {
            // safe for primitives (Guava case or Booleans with Java 8)
            itemType = itemType.box();
        }
        return ParameterizedTypeName.get(optionalTypeStrategy.getClassName(), itemType);
    }

    private TypeName referenceTypeToClassName(ReferenceType refType) {
        BaseObjectTypeDefinition defType = types.definitions().objects().get(refType.type());
        if (defType != null) {
            String packageName = defType.packageName().orElse(types.definitions().defaultPackage());
            return ClassName.get(packageName, refType.type());
        } else {
            ExternalTypeDefinition depType = types.imports().get(refType.type());
            checkNotNull(depType, "Unable to resolve type %s", refType.type());
            return ClassName.bestGuess(depType.external().get("java"));
        }
    }

    private static TypeName primitiveTypeToClassName(PrimitiveType type) {
        switch (type) {
            case STRING:
                return ClassName.get(String.class);
            case DOUBLE:
                return TypeName.DOUBLE;
            case INTEGER:
                return TypeName.INT;
            case BOOLEAN:
                return TypeName.BOOLEAN;
            default:
                throw new IllegalStateException("Unknown primitive type: " + type);
        }
    }

    private static TypeName boxIfPrimitive(TypeName type) {
        if (type.isPrimitive()) {
            return type.box();
        }
        return type;
    }

}

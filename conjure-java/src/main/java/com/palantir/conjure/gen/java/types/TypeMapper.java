/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SetType;
import com.squareup.javapoet.ArrayTypeName;
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
        return type.visit(new ClassNameVisitor());
    }

    public ClassName getOptionalType() {
        return optionalTypeStrategy.getClassName();
    }

    public String getAbsentMethodName() {
        return optionalTypeStrategy.getAbsentMethodName();
    }

    private class ClassNameVisitor implements ConjureTypeVisitor<TypeName> {

        @Override
        public TypeName visit(AnyType type) {
            return ClassName.get(Object.class);
        }

        @Override
        public TypeName visit(ListType type) {
            TypeName itemType = boxIfPrimitive(getClassName(type.itemType()));
            return ParameterizedTypeName.get(ClassName.get(java.util.List.class), itemType);
        }

        @Override
        public TypeName visit(MapType type) {
            return ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                    boxIfPrimitive(getClassName(type.keyType())),
                    boxIfPrimitive(getClassName(type.valueType())));
        }

        @Override
        public TypeName visit(OptionalType type) {
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

        @Override
        public TypeName visit(PrimitiveType type) {
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

        @Override
        public TypeName visit(ReferenceType refType) {
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

        @Override
        public TypeName visit(SetType type) {
            TypeName itemType = boxIfPrimitive(getClassName(type.itemType()));
            return ParameterizedTypeName.get(ClassName.get(java.util.Set.class), itemType);
        }

        @Override
        public TypeName visit(BinaryType binaryType) {
            return ArrayTypeName.of(byte.class);
        }

    }

    private static TypeName boxIfPrimitive(TypeName type) {
        if (type.isPrimitive()) {
            return type.box();
        }
        return type;
    }

}

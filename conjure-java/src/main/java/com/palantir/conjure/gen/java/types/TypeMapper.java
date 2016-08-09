/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SetType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

public final class TypeMapper {

    public enum OptionalTypeStrategy {
        Java8(java.util.Optional.class, "empty"),
        Guava(com.google.common.base.Optional.class, "absent");

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
            return primtiveTypeToClassName((PrimitiveType) type);
        } else if (type instanceof ReferenceType) {
            return referenceTypeToClassName((ReferenceType) type);
        } else {
            throw new IllegalStateException("Unexpected type " + type.getClass());
        }
    }

    public CodeBlock absentOptional() {
        return CodeBlock.of("$T.$N()", optionalTypeStrategy.getClassName(), optionalTypeStrategy.getAbsentMethodName());
    }

    private TypeName getClassNameForMapType(MapType type) {
        return ParameterizedTypeName.get(ClassName.get(java.util.Map.class),
                getClassName(type.keyType()),
                getClassName(type.valueType()));
    }

    private TypeName getClassNameForListType(ListType type) {
        TypeName innerType = getClassName(type.itemType());
        return ParameterizedTypeName.get(ClassName.get(java.util.List.class), innerType);
    }

    private TypeName getClassNameForSetType(SetType type) {
        return ParameterizedTypeName.get(ClassName.get(java.util.Set.class), getClassName(type.itemType()));
    }

    private TypeName getClassNameForOptionalType(OptionalType type) {
        return ParameterizedTypeName.get(optionalTypeStrategy.getClassName(), getClassName(type.itemType()));
    }

    private TypeName referenceTypeToClassName(ReferenceType refType) {
        ObjectTypeDefinition defType = types.definitions().objects().get(refType.type());
        if (defType != null) {
            String packageName = defType.packageName().orElse(types.definitions().defaultPackage());
            return ClassName.get(packageName, refType.type());
        } else {
            ExternalTypeDefinition depType = types.imports().get(refType.type());
            checkNotNull(depType, "Unable to resolve type %s", refType.type());
            return ClassName.bestGuess(depType.external().get("java"));
        }
    }

    private static TypeName primtiveTypeToClassName(PrimitiveType type) {
        switch (type) {
            case STRING:
                return ClassName.get(String.class);
            case DOUBLE:
                return ClassName.get(Double.class);
            case INTEGER:
                return ClassName.get(Integer.class);
            default:
                throw new IllegalStateException("Unknown primitive type: " + type);
        }
    }

}

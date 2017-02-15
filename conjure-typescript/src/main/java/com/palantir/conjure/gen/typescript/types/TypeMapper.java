/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ListType;
import com.palantir.conjure.defs.types.MapType;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.OptionalType;
import com.palantir.conjure.defs.types.PrimitiveType;
import com.palantir.conjure.defs.types.ReferenceType;
import com.palantir.conjure.defs.types.SetType;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import java.util.Set;
import java.util.Stack;

public final class TypeMapper {

    private final String defaultPackage;
    private final TypesDefinition types;

    public TypeMapper(TypesDefinition types, String defaultPackage) {
        this.types = types;
        this.defaultPackage = defaultPackage;
    }

    public TypescriptType getTypescriptType(ConjureType conjureType) {
        return TypescriptType.builder().name(getTypeNameFromConjureType(conjureType)).build();
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public Set<ReferenceType> getReferencedConjureNames(ConjureType conjureType) {
        ImmutableSet.Builder<ReferenceType> result = ImmutableSet.builder();
        Stack<ConjureType> stack = new Stack<>();
        ReferencedNameVisitor visitor = new ReferencedNameVisitor(stack, result);
        stack.add(conjureType);
        while (!stack.isEmpty()) {
            ConjureType poppedConjureType = stack.pop();
            poppedConjureType.visit(visitor);
        }
        return result.build();
    }

    private static class ReferencedNameVisitor implements ConjureTypeVisitor<Void> {

        private final Stack<ConjureType> stack;
        private final Builder<ReferenceType> result;

        ReferencedNameVisitor(
                Stack<ConjureType> stack, ImmutableSet.Builder<ReferenceType> result) {
            this.stack = stack;
            this.result = result;
        }

        @Override
        public Void visit(AnyType anyType) {
            return null;
        }

        @Override
        public Void visit(ListType listType) {
            stack.add(listType.itemType());
            return null;
        }

        @Override
        public Void visit(MapType mapType) {
            stack.add(mapType.keyType());
            stack.add(mapType.valueType());
            return null;
        }

        @Override
        public Void visit(OptionalType optionalType) {
            stack.add(optionalType.itemType());
            return null;
        }

        @Override
        public Void visit(PrimitiveType primitiveType) {
            return null;
        }

        @Override
        public Void visit(ReferenceType referenceType) {
            result.add(referenceType);
            return null;
        }

        @Override
        public Void visit(SetType setType) {
            stack.add(setType.itemType());
            return null;
        }

        @Override
        public Void visit(BinaryType binaryType) {
            return null;
        }

    }

    public String getContainingPackage(ReferenceType name) {
        BaseObjectTypeDefinition defType = types.definitions().objects().get(name.type());
        if (defType != null) {
            if (defType instanceof ObjectTypeDefinition || defType instanceof EnumTypeDefinition) {
                return defType.packageName().orElse(defaultPackage);
            }
            // primitive types for aliases
        }
        // TODO for now assume to generate primitive types for external definitions
        return null;
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private String getTypeNameFromConjureType(ConjureType conjureType) {
        return conjureType.visit(new TypeNameVisitor());
    }

    private class TypeNameVisitor implements ConjureTypeVisitor<String> {

        @Override
        public String visit(AnyType anyType) {
            return "any";
        }

        @Override
        public String visit(ListType listType) {
            return getTypeNameFromConjureType(listType.itemType()) + "[]";
        }

        @Override
        public String visit(MapType mapType) {
            String keyType = getTypeNameFromConjureType(mapType.keyType());
            String valueType = getTypeNameFromConjureType(mapType.valueType());
            return "{ [key: " + keyType + "]: " + valueType + " }";
        }

        @Override
        public String visit(OptionalType optionalType) {
            return getTypeNameFromConjureType(optionalType.itemType());
        }

        @Override
        public String visit(PrimitiveType primitiveType) {
            switch (primitiveType) {
                case DOUBLE:
                case INTEGER:
                    return "number";
                case STRING:
                    return "string";
                case BOOLEAN:
                    return "boolean";
                default:
                    throw new IllegalArgumentException("Unknown primitive type" + primitiveType);
            }
        }

        @Override
        public String visit(ReferenceType refType) {
            BaseObjectTypeDefinition defType = types.definitions().objects().get(refType.type());
            if (defType != null) {
                if (defType instanceof AliasTypeDefinition) {
                    // in typescript we collapse alias types to concrete types
                    return visit(((AliasTypeDefinition) defType).alias());
                } else if (defType instanceof EnumTypeDefinition) {
                    return refType.type();
                } else {
                    // Interfaces are prepended with "I"
                    return "I" + refType.type();
                }
            } else {
                ExternalTypeDefinition depType = types.imports().get(refType.type());
                checkNotNull(depType, "Unable to resolve type %s", refType.type());
                return getTypeNameFromConjureType(depType.baseType());
            }
        }

        @Override
        public String visit(SetType setType) {
            return getTypeNameFromConjureType(setType.itemType()) + "[]";
        }

        @Override
        public String visit(BinaryType binaryType) {
            // TODO(jellis): support this
            throw new RuntimeException("BinaryType not supported by conjure-typescript");
        }

    }
}

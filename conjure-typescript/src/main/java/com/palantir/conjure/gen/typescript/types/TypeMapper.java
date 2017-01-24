/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.defs.types.AliasTypeDefinition;
import com.palantir.conjure.defs.types.AnyType;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
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
        Set<ConjureType> seen = Sets.newHashSet();
        stack.add(conjureType);
        while (!stack.isEmpty()) {
            ConjureType poppedConjureType = stack.pop();
            if (!seen.contains(poppedConjureType)) {
                if (poppedConjureType instanceof ExternalTypeDefinition) {
                    throw new RuntimeException("Should never happen");
                } else if (poppedConjureType instanceof ListType) {
                    stack.add(((ListType) poppedConjureType).itemType());
                } else if (poppedConjureType instanceof MapType) {
                    stack.add(((MapType) poppedConjureType).keyType());
                    stack.add(((MapType) poppedConjureType).valueType());
                } else if (poppedConjureType instanceof ObjectTypeDefinition) {
                    // TODO support this
                    throw new RuntimeException("Not supported");
                } else if (poppedConjureType instanceof OptionalType) {
                    stack.add(((OptionalType) poppedConjureType).itemType());
                } else if (poppedConjureType instanceof ReferenceType) {
                    result.add((ReferenceType) poppedConjureType);
                } else if (poppedConjureType instanceof SetType) {
                    stack.add(((SetType) poppedConjureType).itemType());
                } else if (poppedConjureType instanceof AnyType) {
                    // no-op
                } else {
                    throw new IllegalArgumentException("Unknown conjure type: " + poppedConjureType);
                }
                // PrimitiveType omitted - no op
            }
        }
        return result.build();
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

    private String getTypeNameFromConjureType(ConjureType conjureType) {
        if (conjureType instanceof ExternalTypeDefinition) {
            return getTypeNameFromConjureType(((ExternalTypeDefinition) conjureType).baseType());
        } else if (conjureType instanceof ListType) {
            return getTypeNameFromConjureType(((ListType) conjureType).itemType()) + "[]";
        } else if (conjureType instanceof MapType) {
            String keyType = getTypeNameFromConjureType(((MapType) conjureType).keyType());
            String valueType = getTypeNameFromConjureType(((MapType) conjureType).valueType());
            return "{ [key: " + keyType + "]: " + valueType + " }";
        } else if (conjureType instanceof ObjectTypeDefinition) {
            // TODO support this
            throw new RuntimeException("Not supported");
        } else if (conjureType instanceof OptionalType) {
            return getTypeNameFromConjureType(((OptionalType) conjureType).itemType());
        } else if (conjureType instanceof PrimitiveType) {
            return getPrimitiveTypeName((PrimitiveType) conjureType);
        } else if (conjureType instanceof ReferenceType) {
            return referenceTypeToName((ReferenceType) conjureType);
        } else if (conjureType instanceof SetType) {
            return getTypeNameFromConjureType(((SetType) conjureType).itemType()) + "[]";
        } else if (conjureType instanceof AnyType) {
            return "any";
        }
        throw new IllegalArgumentException("Unknown conjure type: " + conjureType);
    }

    private String getPrimitiveTypeName(PrimitiveType conjureType) {
        switch (conjureType) {
            case DOUBLE:
            case INTEGER:
                return "number";
            case STRING:
                return "string";
            case BOOLEAN:
                return "boolean";
            default:
                throw new IllegalArgumentException("Unknown primitive type" + conjureType);
        }
    }

    private String referenceTypeToName(ReferenceType refType) {
        BaseObjectTypeDefinition defType = types.definitions().objects().get(refType.type());
        if (defType != null) {
            if (defType instanceof AliasTypeDefinition) {
                // in typescript we collapse alias types to concrete types
                return getPrimitiveTypeName(((AliasTypeDefinition) defType).alias());
            } else if (defType instanceof EnumTypeDefinition) {
                return refType.type();
            } else {
                // Interfaces are prepended with "I"
                return "I" + refType.type();
            }
        } else {
            ExternalTypeDefinition depType = types.imports().get(refType.type());
            checkNotNull(depType, "Unable to resolve type %s", refType.type());
            return this.getTypeNameFromConjureType(depType.baseType());
        }
    }
}

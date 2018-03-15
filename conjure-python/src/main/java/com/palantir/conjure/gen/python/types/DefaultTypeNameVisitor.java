/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.google.common.collect.Maps;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import java.util.List;
import java.util.Map;


public final class DefaultTypeNameVisitor implements ConjureTypeVisitor<String> {

    private final Map<TypeName, TypeDefinition> typesByName;

    public DefaultTypeNameVisitor(List<TypeDefinition> types) {
        this.typesByName = Maps.uniqueIndex(types, t -> t.typeName());
    }

    @Override
    public String visitAny(AnyType type) {
        return "object";
    }

    @Override
    public String visitList(ListType type) {
        return "ListType(" + type.itemType().visit(this) + ")";
    }

    @Override
    public String visitMap(MapType type) {
        return "DictType(" + type.keyType().visit(this) + ", " + type.valueType().visit(this) + ")";
    }

    @Override
    public String visitOptional(OptionalType type) {
        return "OptionalType(" + type.itemType().visit(this) + ")";
    }

    @Override
    public String visitPrimitive(PrimitiveType type) {
        switch (type) {
            case STRING:
            case RID:
            case BEARERTOKEN:
            case UUID:
                return "str";
            case BOOLEAN:
                return "bool";
            case DOUBLE:
                return "float";
            case INTEGER:
            case SAFELONG:
                return "int";
            default:
                throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    @Override
    public String visitLocalReference(LocalReferenceType type) {
        // Types without namespace are either defined locally in this conjure definition, or raw imports.
        TypeDefinition baseType = typesByName.get(type.type());

        if (baseType != null) {
            return type.type().name();
        } else {
            throw new IllegalStateException("unknown type: " + type);
        }
    }

    @Override
    public String visitExternal(ExternalType externalType) {
        return visitPrimitive(externalType.fallback());
    }

    @Override
    public String visitSet(SetType type) {
        // TODO (bduffield): real sets
        return ListType.of(type.itemType()).visit(this);
    }

    @Override
    public String visitBinary(BinaryType type) {
        return "BinaryType()";
    }

    @Override
    public String visitDateTime(DateTimeType type) {
        return "str";
    }
}

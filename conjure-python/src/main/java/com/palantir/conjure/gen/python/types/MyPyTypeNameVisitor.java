/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.reference.ForeignReferenceType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;

/**
 * The mypy type for the conjure type.
 */
public final class MyPyTypeNameVisitor implements ConjureTypeVisitor<String> {

    private final TypesDefinition types;

    public MyPyTypeNameVisitor(TypesDefinition types) {
        this.types = types;
    }


    @Override
    public String visitAny(AnyType type) {
        return "Any";
    }

    @Override
    public String visitList(ListType type) {
        return "List[" + type.itemType().visit(this) + "]";
    }

    @Override
    public String visitMap(MapType type) {
        return "Dict[" + type.keyType().visit(this) + ", " + type.valueType().visit(this) + "]";
    }

    @Override
    public String visitOptional(OptionalType type) {
        return "Optional[" + type.itemType().visit(this) + "]";
    }

    @Override
    public String visitPrimitive(PrimitiveType type) {
        switch (type) {
            case STRING:
            case RID:
            case BEARERTOKEN:
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
        BaseObjectTypeDefinition baseType = types.definitions().objects().get(type.type());
        if (baseType != null) {
            return type.type().name();
        } else {
            ExternalTypeDefinition depType = types.imports().get(type.type());
            checkNotNull(depType, "Unable to resolve type %s", type.type());
            return visitPrimitive(depType.baseType());
        }
    }

    @Override
    public String visitForeignReference(ForeignReferenceType type) {
        return type.type().name();
    }

    @Override
    public String visitSet(SetType type) {
        // TODO (bduffield): real sets
        return ListType.of(type.itemType()).visit(this);
    }

    @Override
    public String visitBinary(BinaryType type) {
        return "Any";
    }

    @Override
    public String visitDateTime(DateTimeType type) {
        return "str";
    }

}

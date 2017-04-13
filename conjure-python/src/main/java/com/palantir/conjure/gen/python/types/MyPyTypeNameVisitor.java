/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.builtin.SafeLongType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.reference.ReferenceType;

/**
 * The mypy type for the conjure type.
 */
public final class MyPyTypeNameVisitor implements ConjureTypeVisitor<String> {

    private final TypesDefinition types;

    public MyPyTypeNameVisitor(TypesDefinition types) {
        this.types = types;
    }


    @Override
    public String visit(AnyType anyType) {
        return "Any";
    }

    @Override
    public String visit(ListType listType) {
        return "List[" + listType.itemType().visit(this) + "]";
    }

    @Override
    public String visit(MapType mapType) {
        return "Dict[" + mapType.keyType().visit(this) + ", " + mapType.valueType().visit(this) + "]";
    }

    @Override
    public String visit(OptionalType optionalType) {
        return optionalType.itemType().visit(this);
    }

    @Override
    public String visit(PrimitiveType primitiveType) {
        switch (primitiveType) {
            case STRING:
                return "str";
            case BOOLEAN:
                return "bool";
            case DOUBLE:
                return "float";
            case INTEGER:
                return "int";
            default:
                throw new IllegalArgumentException("unknown type: " + primitiveType);
        }
    }

    @Override
    public String visit(ReferenceType refType) {
        if (!refType.namespace().isPresent()) {
            // Types without namespace are either defined locally in this conjure definition, or raw imports.
            BaseObjectTypeDefinition type = types.definitions().objects().get(refType.type());
            if (type != null) {
                return refType.type().name();
            } else {
                ExternalTypeDefinition depType = types.imports().get(refType.type());
                checkNotNull(depType, "Unable to resolve type %s", refType.type());
                return visit(depType.baseType());
            }
        } else {
            // Types with namespace are imported Conjure types.
            return refType.type().name();
        }
    }

    @Override
    public String visit(SetType setType) {
        // TODO (bduffield): real sets
        return ListType.of(setType.itemType()).visit(this);
    }

    @Override
    public String visit(BinaryType binaryType) {
        return "Any";
    }

    @Override
    public String visit(SafeLongType safeLongType) {
        return "int";
    }

    @Override
    public String visit(DateTimeType dateTimeType) {
        return "str";
    }

}

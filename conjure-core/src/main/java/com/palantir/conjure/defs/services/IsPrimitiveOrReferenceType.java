/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;

public final class IsPrimitiveOrReferenceType implements ConjureTypeVisitor<Boolean> {
    public static final IsPrimitiveOrReferenceType INSTANCE = new IsPrimitiveOrReferenceType();

    @Override
    public Boolean visitAny(AnyType type) {
        return false;
    }

    @Override
    public Boolean visitList(ListType type) {
        return false;
    }

    @Override
    public Boolean visitMap(MapType type) {
        return false;
    }

    @Override
    public Boolean visitOptional(OptionalType type) {
        return false;
    }

    @Override
    public Boolean visitPrimitive(PrimitiveType type) {
        return true;
    }

    @Override
    public Boolean visitLocalReference(LocalReferenceType type) {
        return true;
    }

    @Override
    public Boolean visitSet(SetType type) {
        return false;
    }

    @Override
    public Boolean visitBinary(BinaryType type) {
        return false;
    }

    @Override
    public Boolean visitDateTime(DateTimeType type) {
        return true;
    }
}

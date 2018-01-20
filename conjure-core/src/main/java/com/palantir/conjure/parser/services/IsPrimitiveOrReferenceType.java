/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.services;

import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.builtin.DateTimeType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;

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
    public Boolean visitForeignReference(ForeignReferenceType type) {
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

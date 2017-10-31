/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ForeignReferenceType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;

public interface ConjureTypeVisitor<T> {
    T visit(AnyType type);
    T visit(ListType type);
    T visit(MapType type);
    T visit(OptionalType type);
    T visit(PrimitiveType type);
    T visit(LocalReferenceType type);
    T visit(ForeignReferenceType type);
    T visit(SetType type);
    T visit(BinaryType type);
    T visit(DateTimeType type);
}

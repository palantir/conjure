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
import com.palantir.conjure.defs.types.reference.LocalReferenceType;

public interface ConjureTypeVisitor<T> {
    T visitAny(AnyType type);
    T visitList(ListType type);
    T visitMap(MapType type);
    T visitOptional(OptionalType type);
    T visitPrimitive(PrimitiveType type);
    T visitLocalReference(LocalReferenceType type);
    T visitSet(SetType type);
    T visitBinary(BinaryType type);
    T visitDateTime(DateTimeType type);
}

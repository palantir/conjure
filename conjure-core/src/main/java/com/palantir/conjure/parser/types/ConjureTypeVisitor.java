/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types;

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

public interface ConjureTypeVisitor<T> {
    T visitAny(AnyType type);
    T visitList(ListType type);
    T visitMap(MapType type);
    T visitOptional(OptionalType type);
    T visitPrimitive(PrimitiveType type);
    T visitLocalReference(LocalReferenceType type);
    T visitForeignReference(ForeignReferenceType type);
    T visitSet(SetType type);
    T visitBinary(BinaryType type);
    T visitDateTime(DateTimeType type);
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

public interface ConjureTypeVisitor<T> {

    T visit(AnyType anyType);

    T visit(ListType listType);

    T visit(MapType mapType);

    T visit(OptionalType optionalType);

    T visit(PrimitiveType primitiveType);

    T visit(ReferenceType referenceType);

    T visit(SetType setType);

    T visit(BinaryType binaryType);

    T visit(SafeLongType safeLongType);

    T visit(DateTimeType dateTimeType);

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.builtin.SafeLongType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ReferenceType;

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

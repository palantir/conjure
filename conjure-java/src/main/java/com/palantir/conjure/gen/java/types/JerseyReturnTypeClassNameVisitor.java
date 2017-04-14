/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

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
import com.palantir.conjure.defs.types.reference.ReferenceType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public final class JerseyReturnTypeClassNameVisitor implements ClassNameVisitor {

    private final DefaultClassNameVisitor delegate;

    public JerseyReturnTypeClassNameVisitor(TypesDefinition types) {
        this.delegate = new DefaultClassNameVisitor(types);
    }

    @Override
    public TypeName visit(AnyType anyType) {
        return delegate.visit(anyType);
    }

    @Override
    public TypeName visit(ListType listType) {
        return delegate.visit(listType);
    }

    @Override
    public TypeName visit(MapType mapType) {
        return delegate.visit(mapType);
    }

    @Override
    public TypeName visit(OptionalType optionalType) {
        return delegate.visit(optionalType);
    }

    @Override
    public TypeName visit(PrimitiveType primitiveType) {
        return delegate.visit(primitiveType);
    }

    @Override
    public TypeName visit(ReferenceType referenceType) {
        return delegate.visit(referenceType);
    }

    @Override
    public TypeName visit(SetType setType) {
        return delegate.visit(setType);
    }

    @Override
    public TypeName visit(SafeLongType safeLongType) {
        return delegate.visit(safeLongType);
    }

    @Override
    public TypeName visit(BinaryType binaryType) {
        return ClassName.get(javax.ws.rs.core.StreamingOutput.class);
    }

    @Override
    public TypeName visit(DateTimeType dateTimeType) {
        return delegate.visit(dateTimeType);
    }

}

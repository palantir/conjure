/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.types.TypesDefinition;
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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.io.InputStream;

public final class JerseyMethodTypeClassNameVisitor implements ClassNameVisitor {

    private final DefaultClassNameVisitor delegate;

    public JerseyMethodTypeClassNameVisitor(TypesDefinition types) {
        this.delegate = new DefaultClassNameVisitor(types);
    }

    @Override
    public TypeName visit(AnyType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(ListType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(MapType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(OptionalType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(PrimitiveType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(LocalReferenceType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(ForeignReferenceType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(SetType type) {
        return delegate.visit(type);
    }

    @Override
    public TypeName visit(BinaryType type) {
        return ClassName.get(InputStream.class);
    }

    @Override
    public TypeName visit(DateTimeType type) {
        return delegate.visit(type);
    }

}

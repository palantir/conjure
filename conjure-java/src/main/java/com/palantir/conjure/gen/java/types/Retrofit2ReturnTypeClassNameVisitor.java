/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import com.palantir.conjure.defs.types.builtin.DateTimeType;
import com.palantir.conjure.defs.types.collect.ListType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.collect.OptionalType;
import com.palantir.conjure.defs.types.collect.SetType;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import com.palantir.conjure.defs.types.reference.ExternalType;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.List;

public final class Retrofit2ReturnTypeClassNameVisitor implements ClassNameVisitor {

    private static final ClassName RESPONSE_BODY_TYPE = ClassName.get("okhttp3", "ResponseBody");

    private final DefaultClassNameVisitor delegate;

    public Retrofit2ReturnTypeClassNameVisitor(List<TypeDefinition> types) {
        this.delegate = new DefaultClassNameVisitor(types);
    }

    @Override
    public TypeName visitAny(AnyType type) {
        return delegate.visitAny(type);
    }

    @Override
    public TypeName visitList(ListType type) {
        return delegate.visitList(type);
    }

    @Override
    public TypeName visitMap(MapType type) {
        return delegate.visitMap(type);
    }

    @Override
    public TypeName visitOptional(OptionalType type) {
        return delegate.visitOptional(type);
    }

    @Override
    public TypeName visitPrimitive(PrimitiveType type) {
        return delegate.visitPrimitive(type);
    }

    @Override
    public TypeName visitLocalReference(LocalReferenceType type) {
        return delegate.visitLocalReference(type);
    }

    @Override
    public TypeName visitExternal(ExternalType type) {
        return delegate.visitExternal(type);
    }

    @Override
    public TypeName visitSet(SetType type) {
        return delegate.visitSet(type);
    }

    @Override
    public TypeName visitBinary(BinaryType type) {
        return RESPONSE_BODY_TYPE;
    }

    @Override
    public TypeName visitDateTime(DateTimeType type) {
        return delegate.visitDateTime(type);
    }

}

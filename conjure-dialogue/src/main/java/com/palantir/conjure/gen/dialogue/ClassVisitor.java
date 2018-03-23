/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

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
import com.palantir.conjure.gen.java.types.ClassNameVisitor;
import com.palantir.conjure.gen.java.types.DefaultClassNameVisitor;
import com.squareup.javapoet.TypeName;
import java.io.InputStream;
import java.util.List;

public final class ClassVisitor implements ClassNameVisitor {

    enum Mode {
        RETURN_VALUE,
        PARAMETER
    }

    private final DefaultClassNameVisitor delegate;
    private final Mode mode;

    public ClassVisitor(List<TypeDefinition> types, Mode mode) {
        this.delegate = new DefaultClassNameVisitor(types);
        this.mode = mode;
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
        throw new UnsupportedOperationException("TYpe is not supported by conjure-dialogue: " + type);
    }

    @Override
    public TypeName visitSet(SetType type) {
        return delegate.visitSet(type);
    }

    @Override
    public TypeName visitBinary(BinaryType type) {
        if (mode == Mode.RETURN_VALUE) {
            return TypeName.get(InputStream.class);
        } else {
            throw new UnsupportedOperationException("BinaryType is not supported in conjure-dialogue mode: " + mode);
        }
    }

    @Override
    public TypeName visitDateTime(DateTimeType type) {
        return delegate.visitDateTime(type);
    }

}

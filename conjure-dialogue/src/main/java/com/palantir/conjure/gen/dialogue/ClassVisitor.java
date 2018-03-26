/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

import com.palantir.conjure.gen.java.types.ClassNameVisitor;
import com.palantir.conjure.gen.java.types.DefaultClassNameVisitor;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.TypeDefinition;
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
    public TypeName visitPrimitive(PrimitiveType value) {
        if (!value.equals(PrimitiveType.BINARY)) {
            return delegate.visitPrimitive(value);
        }

        if (mode == Mode.RETURN_VALUE) {
            return TypeName.get(InputStream.class);
        } else {
            throw new UnsupportedOperationException("BinaryType is not supported in conjure-dialogue mode: " + mode);
        }
    }

    @Override
    public TypeName visitOptional(OptionalType value) {
        return delegate.visitOptional(value);
    }

    @Override
    public TypeName visitList(ListType value) {
        return delegate.visitList(value);
    }

    @Override
    public TypeName visitSet(SetType value) {
        return delegate.visitSet(value);
    }

    @Override
    public TypeName visitMap(MapType value) {
        return delegate.visitMap(value);
    }

    @Override
    public TypeName visitReference(com.palantir.conjure.spec.TypeName value) {
        return delegate.visitReference(value);
    }

    @Override
    public TypeName visitExternal(ExternalReference value) {
        throw new UnsupportedOperationException("TYpe is not supported by conjure-dialogue: " + value);
    }
}

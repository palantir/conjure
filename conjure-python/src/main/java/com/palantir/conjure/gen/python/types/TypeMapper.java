/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;


import com.palantir.conjure.spec.Type;

public final class TypeMapper {

    private final Type.Visitor<String> typeNameVisitor;

    public TypeMapper(Type.Visitor<String> typeNameVisitor) {
        this.typeNameVisitor = typeNameVisitor;
    }

    public String getTypeName(Type type) {
        return type.accept(typeNameVisitor);
    }
}

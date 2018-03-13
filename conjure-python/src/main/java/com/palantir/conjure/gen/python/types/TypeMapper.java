/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python.types;

import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.Type;

public final class TypeMapper {

    private final ConjureTypeVisitor<String> typeNameVisitor;

    public TypeMapper(ConjureTypeVisitor<String> typeNameVisitor) {
        this.typeNameVisitor = typeNameVisitor;
    }

    public String getTypeName(Type type) {
        return type.visit(typeNameVisitor);
    }
}

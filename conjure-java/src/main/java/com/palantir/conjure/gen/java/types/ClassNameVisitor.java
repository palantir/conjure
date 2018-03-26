/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.squareup.javapoet.TypeName;
import java.util.List;

/**
 * Maps a conjure type into the corresponding java type.
 */
public interface ClassNameVisitor extends Type.Visitor<TypeName> {

    interface Factory {
        ClassNameVisitor create(List<TypeDefinition> types);
    }

    @Override
    default TypeName visitUnknown(String unknownType) {
        throw new IllegalStateException("Unknown type:" + unknownType);
    }
}

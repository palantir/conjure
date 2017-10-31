/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.squareup.javapoet.TypeName;

/**
 * Maps a conjure type into the corresponding java type.
 */
public interface ClassNameVisitor extends ConjureTypeVisitor<TypeName> {

    interface Factory {
        ClassNameVisitor create(TypesDefinition types);
    }
}

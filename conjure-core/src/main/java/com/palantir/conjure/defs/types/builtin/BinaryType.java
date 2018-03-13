/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.builtin;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.Type;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface BinaryType extends Type {

    // marker interface

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitBinary(this);
    }

    static BinaryType of() {
        return ImmutableBinaryType.builder().build();
    }

}

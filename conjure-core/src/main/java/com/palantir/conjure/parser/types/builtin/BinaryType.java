/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.builtin;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface BinaryType extends ConjureType {

    // marker interface

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitBinary(this);
    }

    static BinaryType of() {
        return ImmutableBinaryType.builder().build();
    }

}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.builtin;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface SafeLongType extends ConjureType {

    // marker interface

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static SafeLongType of() {
        return ImmutableSafeLongType.builder().build();
    }

}

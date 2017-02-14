/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface BinaryType extends ConjureType {

    // marker interface

    static BinaryType of() {
        return ImmutableBinaryType.builder().build();
    }

}

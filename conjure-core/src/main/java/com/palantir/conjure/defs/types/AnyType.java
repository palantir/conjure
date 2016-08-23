/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface AnyType extends ConjureType {

    // marker interface

    static AnyType of() {
        return ImmutableAnyType.builder().build();
    }

}

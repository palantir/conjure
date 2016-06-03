/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface OptionalType extends ConjureType {

    ConjureType itemType();

    static OptionalType of(ConjureType itemType) {
        return ImmutableOptionalType.builder().itemType(itemType).build();
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ListType extends ConjureType {

    ConjureType itemType();

    static ListType of(ConjureType itemType) {
        return ImmutableListType.builder().itemType(itemType).build();
    }

}

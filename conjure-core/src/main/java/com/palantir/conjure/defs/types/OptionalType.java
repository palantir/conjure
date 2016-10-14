/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface OptionalType extends ConjureType {

    @JsonProperty("item-type")
    ConjureType itemType();

    static OptionalType of(ConjureType itemType) {
        return ImmutableOptionalType.builder().itemType(itemType).build();
    }

}

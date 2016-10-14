/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface SetType extends ConjureType {

    @JsonProperty("item-type")
    ConjureType itemType();

    static SetType of(ConjureType itemType) {
        return ImmutableSetType.builder().itemType(itemType).build();
    }

}

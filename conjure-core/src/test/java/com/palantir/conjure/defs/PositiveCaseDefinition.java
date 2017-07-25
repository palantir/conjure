/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutablePositiveCaseDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface PositiveCaseDefinition {
    @JsonProperty("conjure")
    Object conjure();
}

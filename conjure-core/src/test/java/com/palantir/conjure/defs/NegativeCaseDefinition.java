/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableNegativeCaseDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface NegativeCaseDefinition {
    @JsonProperty("expected-error")
    String expectedError();

    @JsonProperty("conjure")
    Object conjure();
}

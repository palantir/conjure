/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import java.util.SortedMap;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableTestCaseDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface TestCaseDefinition {
    @JsonProperty("test-case-name")
    String testCaseName();

    @JsonProperty("negative")
    Optional<SortedMap<String, NegativeCaseDefinition>> negative();

    @JsonProperty("positive")
    Optional<SortedMap<String, PositiveCaseDefinition>> positive();
}

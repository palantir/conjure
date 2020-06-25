/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.NavigableMap;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableTestCaseDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface TestCaseDefinition {
    @JsonProperty("test-case-name")
    String testCaseName();

    @JsonProperty("negative")
    Optional<NavigableMap<String, NegativeCaseDefinition>> negative();

    @JsonProperty("positive")
    Optional<NavigableMap<String, PositiveCaseDefinition>> positive();
}

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

package com.palantir.conjure.parser.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.ConjureMetrics;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface RequestLineDefinition {

    String method();

    PathDefinition path();

    static RequestLineDefinition of(String method, PathDefinition path) {
        return ImmutableRequestLineDefinition.builder().method(method).path(path).build();
    }

    @JsonCreator
    static RequestLineDefinition valueOf(String oneline) {
        String[] parts = oneline.split(" ", 2);
        checkArgument(parts.length == 2,
                "Request line must be of the form: [METHOD] [PATH], instead was '%s'",
                oneline);
        ConjureMetrics.incrementCounter(RequestLineDefinition.class, "method", parts[0]);
        return of(parts[0], PathDefinition.of(parts[1]));
    }

    @JsonValue
    default String asString() {
        return String.format("%s %s", method(), path());
    }
}

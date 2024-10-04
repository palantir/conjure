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
import com.palantir.logsafe.Unsafe;
import org.immutables.value.Value;

@Unsafe
@Value.Immutable
@ConjureImmutablesStyle
public interface RequestLineDefinition {

    String method();

    PathString path();

    static RequestLineDefinition of(String method, PathString path) {
        return ImmutableRequestLineDefinition.builder()
                .method(method)
                .path(path)
                .build();
    }

    @JsonCreator
    static RequestLineDefinition valueOf(String oneline) {
        String[] parts = oneline.split(" ", 2);
        checkArgument(
                parts.length == 2, "Request line must be of the form: [METHOD] [PATH], instead was '%s'", oneline);
        return of(parts[0], PathString.of(parts[1]));
    }

    @JsonValue
    default String asString() {
        return String.format("%s %s", method(), path());
    }
}

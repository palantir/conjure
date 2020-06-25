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

package com.palantir.conjure.parser.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;
import org.immutables.value.Value.Auxiliary;

/**
 * Represents the code of a conjure {@link ErrorTypeDefinition#code() error}. Must be the UpperCamel version of one of
 * the {@link com.palantir.conjure.spec.ErrorCode error codes}.
 */
@JsonDeserialize(as = ImmutableErrorCode.class)
@Value.Immutable
@ConjureImmutablesStyle
public abstract class ErrorCode {

    private static final Set<String> VALID_ERROR_CODE_NAMES = Arrays.stream(
                    com.palantir.conjure.spec.ErrorCode.Value.values())
            .map(Enum::name)
            .filter(code -> !code.equals("UNKNOWN"))
            .collect(Collectors.toSet());

    @JsonValue
    public abstract String name();

    @Auxiliary
    public final com.palantir.conjure.spec.ErrorCode asSpecErrorCode() {
        return com.palantir.conjure.spec.ErrorCode.valueOf(name());
    }

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                VALID_ERROR_CODE_NAMES.contains(name()),
                "Invalid error code %s. Must be one of: %s",
                name(),
                VALID_ERROR_CODE_NAMES);
    }

    @JsonCreator
    public static ErrorCode of(String name) {
        return ImmutableErrorCode.builder().name(name).build();
    }
}

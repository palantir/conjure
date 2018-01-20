/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.remoting.api.errors.ErrorType;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

/**
 * Represents the code of a conjure {@link ErrorTypeDefinition#code() error}. Must be the UpperCamel version of one of
 * the http-remoting {@link ErrorType.Code error codes}.
 */
@JsonDeserialize(as = ImmutableErrorCode.class)
@Value.Immutable
@ConjureImmutablesStyle
public abstract class ErrorCode {

    private static final Set<String> VALID_ERROR_CODE_NAMES = Arrays.stream(ErrorType.Code.values())
            .map(ErrorType.Code::name)
            .collect(Collectors.toSet());

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(VALID_ERROR_CODE_NAMES.contains(name()),
                "Invalid error code %s. Must be one of: %s", name(), VALID_ERROR_CODE_NAMES);
    }

    @JsonCreator
    public static ErrorCode of(String name) {
        return ImmutableErrorCode.builder().name(name).build();
    }
}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of an argument in an {@link EndpointDefinition}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class ParameterName {

    public static final String PATTERN = "[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*";
    public static final Pattern ANCHORED_PATTERN = Pattern.compile("^" + PATTERN + "$");
    public static final Pattern HEADER_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*(-[A-Z][a-zA-Z0-9]*)*$");

    @JsonValue
    public abstract String name();

    @JsonCreator
    public static ParameterName of(String name) {
        return ImmutableParameterName.builder().name(name).build();
    }

    @Override
    public final String toString() {
        return name();
    }
}

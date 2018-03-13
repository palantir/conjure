/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of an argument in an {@link EndpointDefinition}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class ArgumentName {

    public static final String PATTERN = "[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*";
    public static final Pattern ANCHORED_PATTERN = Pattern.compile("^" + PATTERN + "$");

    public abstract String name();

    public static ArgumentName of(String name) {
        return ImmutableArgumentName.builder().name(name).build();
    }

    @Override
    public final String toString() {
        return name();
    }
}

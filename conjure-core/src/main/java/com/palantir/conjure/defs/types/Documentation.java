/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

/**
 * Represents the documentation of a Conjure definition.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class Documentation {

    @JsonValue
    public abstract String value();

    @Override
    public final String toString() {
        return value().toString();
    }

    public static Documentation of(String value) {
        return ImmutableDocumentation.builder().value(value).build();
    }
}

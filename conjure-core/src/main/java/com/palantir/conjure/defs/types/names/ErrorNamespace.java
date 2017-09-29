/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the namespace of a Conjure {@link ErrorTypeDefinition#code() error}.
 */
@JsonDeserialize(as = ImmutableErrorNamespace.class)
@Value.Immutable
@ConjureImmutablesStyle
public abstract class ErrorNamespace {

    private static final Pattern UPPER_CAMEL_CASE = Pattern.compile("(([A-Z][a-z0-9]+)+)");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(UPPER_CAMEL_CASE.matcher(name()).matches(),
                "Namespace for errors must match pattern %s: %s", UPPER_CAMEL_CASE, name());
    }

    @JsonCreator
    public static ErrorNamespace of(String name) {
        return ImmutableErrorNamespace.builder().name(name).build();
    }

}

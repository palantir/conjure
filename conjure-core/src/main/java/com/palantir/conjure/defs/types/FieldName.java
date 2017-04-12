/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

/**
 * Represents the name of an {@link ObjectTypeDefinition#fields() field} of an {@link ObjectTypeDefinition}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class FieldName {

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        // TODO(rfink): Introduce syntax checking.
    }

    @JsonCreator
    public static FieldName of(String name) {
        return ImmutableFieldName.builder().name(name).build();
    }
}

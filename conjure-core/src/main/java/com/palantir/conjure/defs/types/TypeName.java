/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ObjectsDefinition;
import javax.annotation.concurrent.Immutable;
import org.immutables.value.Value;

/**
 * Represents the name of a conjure {@link ObjectsDefinition#objects() object}.
 * TODO
 */
@Value.Immutable
@ConjureImmutablesStyle
@Immutable
public abstract class TypeName {

    public static final TypeName UNKNOWN = TypeName.of("<UNKNOWN>");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        // TODO(rfink): Introduce syntax checking.
    }

    @JsonCreator
    public static TypeName of(String name) {
        return ImmutableTypeName.builder().name(name).build();
    }
}

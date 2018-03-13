/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public abstract class EndpointName {

    public abstract String name();

    public static EndpointName of(String name) {
        return ImmutableEndpointName.builder().name(name).build();
    }

    @Override
    public final String toString() {
        return name();
    }
}

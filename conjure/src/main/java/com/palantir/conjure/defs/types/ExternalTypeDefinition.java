/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableExternalTypeDefinition.class)
@Value.Immutable
public interface ExternalTypeDefinition {

    String external();

    Optional<ConjureType> fallback();

    static ExternalTypeDefinition of(String external) {
        return ImmutableExternalTypeDefinition.builder().external(external).build();
    }

    static ExternalTypeDefinition of(String external, ConjureType fallback) {
        return ImmutableExternalTypeDefinition.builder().external(external).fallback(fallback).build();
    }

}

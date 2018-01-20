/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.services;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEndpointDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    RequestLineDefinition http();

    Optional<AuthDefinition> auth();

    Map<ParameterName, ArgumentDefinition> args();

    Set<ConjureType> markers();

    Optional<ConjureType> returns();

    Optional<String> docs();

    Optional<String> deprecated();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableEndpointDefinition.class)
@JsonSerialize(as = ImmutableEndpointDefinition.class)
@Value.Immutable
public interface EndpointDefinition {

    RequestLineDefinition http();

    Optional<AuthDefinition> auth();

    Optional<Map<String, ArgumentDefinition>> args();

    Optional<ConjureType> returns();

    Optional<String> docs();

    // TODO(melliot) verify args and request line match

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}

}

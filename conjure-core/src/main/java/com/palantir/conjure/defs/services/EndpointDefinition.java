/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEndpointDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    @JsonProperty("http")
    RequestLineDefinition http();

    @JsonProperty("auth")
    Optional<AuthDefinition> auth();

    @JsonProperty("args")
    Optional<Map<String, ArgumentDefinition>> args();

    @JsonProperty("returns")
    Optional<ConjureType> returns();

    @JsonProperty("docs")
    Optional<String> docs();

    @JsonProperty("deprecated")
    Optional<String> deprecated();

    // TODO(melliot) verify args and request line match

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}

}

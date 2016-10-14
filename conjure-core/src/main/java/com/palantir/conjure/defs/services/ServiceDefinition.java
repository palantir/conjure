/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableServiceDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ServiceDefinition {

    @JsonProperty("name")
    String name();

    @JsonProperty("package")
    String packageName();

    @JsonProperty("docs")
    Optional<String> docs();

    @JsonProperty("default-auth")
    @Value.Default
    default AuthDefinition defaultAuth() {
        return AuthDefinition.none();
    }

    @JsonProperty("base-path")
    @Value.Default
    default String basePath() {
        return "/";
    }

    @JsonProperty("endpoints")
    Map<String, EndpointDefinition> endpoints();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableServiceDefinition.Builder {}

}

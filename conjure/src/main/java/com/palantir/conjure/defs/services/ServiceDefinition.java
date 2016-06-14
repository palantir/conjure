/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableServiceDefinition.class)
@JsonSerialize(as = ImmutableServiceDefinition.class)
@Value.Immutable
public interface ServiceDefinition {

    String name();

    @JsonProperty("package")
    String packageName();

    Optional<String> docs();

    @Value.Default
    default AuthorizationDefinition defaultAuthz() {
        return AuthorizationDefinition.none();
    }

    @Value.Default
    default String basePath() {
        return "/";
    }

    Map<String, EndpointDefinition> endpoints();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableServiceDefinition.Builder {}

}

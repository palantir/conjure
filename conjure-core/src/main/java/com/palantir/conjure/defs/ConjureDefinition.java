/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.services.ServiceDefinition;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableConjureDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureDefinition {

    @JsonProperty("types")
    TypesDefinition types();

    @JsonProperty("services")
    Map<String, ServiceDefinition> services();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableConjureDefinition.Builder {}

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableTypesDefinition.class)
@JsonSerialize(as = ImmutableTypesDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface TypesDefinition {

    @JsonProperty("imports")
    Map<String, ExternalTypeDefinition> imports();

    @JsonProperty("definitions")
    ObjectsDefinition definitions();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}

}

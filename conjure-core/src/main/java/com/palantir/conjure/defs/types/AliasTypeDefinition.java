/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableAliasTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface AliasTypeDefinition extends BaseObjectTypeDefinition {

    @JsonProperty("alias")
    PrimitiveType alias();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}

}

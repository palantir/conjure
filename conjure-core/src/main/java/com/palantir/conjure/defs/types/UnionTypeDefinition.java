/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.validators.UnionTypeDefinitionValidator;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableUnionTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface UnionTypeDefinition extends BaseObjectTypeDefinition {

    @JsonProperty("union")
    Map<String, UnionMemberTypeDefinition> union();

    @Value.Check
    default void check() {
        for (UnionTypeDefinitionValidator validator : UnionTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableUnionTypeDefinition.Builder {}

}

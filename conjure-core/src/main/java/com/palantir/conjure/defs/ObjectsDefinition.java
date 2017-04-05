/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.validators.ObjectsDefinitionValidator;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectsDefinition.class)
@JsonSerialize(as = ImmutableObjectsDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectsDefinition {

    @JsonProperty("default-package")
    Optional<String> defaultPackage();

    @JsonProperty("objects")
    Map<String, BaseObjectTypeDefinition> objects();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectsDefinition.Builder {}

    @Value.Check
    default void check() {
        for (ObjectsDefinitionValidator validator : ObjectsDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

}

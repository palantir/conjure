/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectsDefinition.class)
@JsonSerialize(as = ImmutableObjectsDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectsDefinition {

    @JsonProperty("default-package")
    Optional<ConjurePackage> defaultConjurePackage();

    // TODO(rfink): Rename to "types" [backcompat break], https://github.palantir.build/foundry/conjure/issues/351
    Map<TypeName, BaseObjectTypeDefinition> objects();

    Map<TypeName, ErrorTypeDefinition> errors();

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

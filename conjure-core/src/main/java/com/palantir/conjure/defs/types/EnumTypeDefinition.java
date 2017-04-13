/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.validators.ConjureValidator;
import com.palantir.conjure.defs.validators.EnumTypeDefinitionValidator;
import java.util.List;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEnumTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EnumTypeDefinition extends BaseObjectTypeDefinition {

    List<EnumValueDefinition> values();

    @Value.Check
    default void check() {
        for (ConjureValidator<EnumTypeDefinition> validator : EnumTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumTypeDefinition.Builder {}
}

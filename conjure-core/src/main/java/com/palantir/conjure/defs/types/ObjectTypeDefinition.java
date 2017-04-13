/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.validators.ObjectTypeDefinitionValidator;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectTypeDefinition extends BaseObjectTypeDefinition {

    Map<FieldName, FieldDefinition> fields();

    @Value.Check
    default void check() {
        for (ObjectTypeDefinitionValidator validator : ObjectTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectTypeDefinition.Builder {}

}

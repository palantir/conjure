/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.FieldName;
import java.util.Map;
import org.immutables.value.Value;

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

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
public interface UnionTypeDefinition extends BaseObjectTypeDefinition {

    Map<FieldName, FieldDefinition> union();

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

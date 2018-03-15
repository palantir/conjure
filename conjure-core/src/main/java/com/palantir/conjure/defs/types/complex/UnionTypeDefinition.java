/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.TypeDefinition;
import java.util.List;
import org.immutables.value.Value;


@Value.Immutable
@ConjureImmutablesStyle
public interface UnionTypeDefinition extends TypeDefinition {

    List<FieldDefinition> union();

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

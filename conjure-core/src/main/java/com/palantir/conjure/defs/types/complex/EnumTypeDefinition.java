/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.TypeDefinition;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface EnumTypeDefinition extends TypeDefinition {

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

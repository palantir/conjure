/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ConjureValidator;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface EnumValueDefinition {

    String value();

    Optional<String> docs();

    @Value.Check
    default void check() {
        for (ConjureValidator<EnumValueDefinition> validator : EnumValueDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static EnumValueDefinition.Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumValueDefinition.Builder {}
}

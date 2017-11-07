/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureValidator;
import java.util.Set;

@com.google.errorprone.annotations.Immutable
public enum EnumTypeDefinitionValidator implements ConjureValidator<EnumTypeDefinition> {
    UniqueEnumValues(new UniqueEnumValuesValidator());

    private final ConjureValidator<EnumTypeDefinition> validator;

    EnumTypeDefinitionValidator(ConjureValidator<EnumTypeDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EnumTypeDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueEnumValuesValidator implements ConjureValidator<EnumTypeDefinition> {

        @Override
        public void validate(EnumTypeDefinition definition) {
            Set<String> enumValues = Sets.newHashSet();
            for (EnumValueDefinition valueDef : definition.values()) {
                boolean unseen = enumValues.add(valueDef.value());
                Preconditions.checkArgument(unseen, "Cannot declare a %s with duplicate enum values: %s",
                        EnumTypeDefinition.class.getSimpleName(), valueDef.value());
            }
        }
    }
}

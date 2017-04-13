/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validators;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.types.EnumTypeDefinition;
import com.palantir.conjure.defs.types.EnumValueDefinition;
import java.util.Set;

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

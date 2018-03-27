/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import java.util.Set;

@com.google.errorprone.annotations.Immutable
public enum EnumDefinitionValidator implements ConjureValidator<EnumDefinition> {
    UniqueEnumValues(new UniqueEnumValuesValidator()),
    ValuesValidator(new ValuesValidator());

    public static void validateAll(EnumDefinition definition) {
        for (EnumDefinitionValidator validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<EnumDefinition> validator;

    EnumDefinitionValidator(ConjureValidator<EnumDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EnumDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueEnumValuesValidator implements ConjureValidator<EnumDefinition> {

        @Override
        public void validate(EnumDefinition definition) {
            Set<String> enumValues = Sets.newHashSet();
            for (EnumValueDefinition valueDef : definition.getValues()) {
                boolean unseen = enumValues.add(valueDef.getValue());
                Preconditions.checkArgument(unseen, "Cannot declare a %s with duplicate enum values: %s",
                        EnumTypeDefinition.class.getSimpleName(), valueDef.getValue());
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ValuesValidator implements ConjureValidator<EnumDefinition> {

        @Override
        public void validate(EnumDefinition definition) {
            definition.getValues().forEach(EnumValueDefinitionValidator::validateAll);
        }
    }

}

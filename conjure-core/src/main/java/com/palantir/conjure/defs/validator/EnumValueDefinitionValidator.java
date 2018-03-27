/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.EnumValueDefinition;
import java.util.regex.Pattern;

@com.google.errorprone.annotations.Immutable
public enum EnumValueDefinitionValidator implements ConjureValidator<EnumValueDefinition> {
    UnknownValueNotUsed(new UnknownValueNotUsedValidator()),
    Format(new FormatValidator());

    public static void validateAll(EnumValueDefinition definition) {
        for (ConjureValidator<EnumValueDefinition> validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<EnumValueDefinition> validator;

    EnumValueDefinitionValidator(ConjureValidator<EnumValueDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EnumValueDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UnknownValueNotUsedValidator implements ConjureValidator<EnumValueDefinition> {

        @Override
        public void validate(EnumValueDefinition definition) {
            Preconditions.checkArgument(!definition.getValue().equalsIgnoreCase("UNKNOWN"),
                    "UNKNOWN is a reserved enumeration value and cannot be used in an %s",
                    EnumValueDefinition.class.getSimpleName());
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class FormatValidator implements ConjureValidator<EnumValueDefinition> {
        private static final Pattern REQUIRED_FORMAT = Pattern.compile("[A-Z][A-Z0-9]*(_[A-Z0-9]+)*");

        @Override
        public void validate(EnumValueDefinition definition) {
            Preconditions.checkArgument(REQUIRED_FORMAT.matcher(definition.getValue()).matches(),
                    "Enumeration values must match format %s: %s", REQUIRED_FORMAT, definition.getValue());
        }
    }
}

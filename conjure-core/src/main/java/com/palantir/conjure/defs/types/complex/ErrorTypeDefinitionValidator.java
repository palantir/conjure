/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@com.google.errorprone.annotations.Immutable
public enum ErrorTypeDefinitionValidator implements ConjureValidator<ErrorDefinition> {
    UNIQUE_ARG_NAMES(definition -> {
        UniqueFieldNamesValidator uniqueFieldNamesValidator = new UniqueFieldNamesValidator(ErrorDefinition.class);
        uniqueFieldNamesValidator.validate(
                Stream.concat(definition.getSafeArgs().stream(), definition.getUnsafeArgs().stream())
                        .map(FieldDefinition::getFieldName).collect(Collectors.toSet()));
    });

    public static void validateAll(ErrorDefinition definition) {
        for (ErrorTypeDefinitionValidator validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<ErrorDefinition> validator;

    ErrorTypeDefinitionValidator(ConjureValidator<ErrorDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ErrorDefinition definition) {
        this.validator.validate(definition);
    }

}

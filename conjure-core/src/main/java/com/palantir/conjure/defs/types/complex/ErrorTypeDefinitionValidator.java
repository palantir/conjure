/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureValidator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@com.google.errorprone.annotations.Immutable
public enum ErrorTypeDefinitionValidator implements ConjureValidator<ErrorTypeDefinition> {
    UNIQUE_ARG_NAMES(definition -> {
        UniqueFieldNamesValidator uniqueFieldNamesValidator = new UniqueFieldNamesValidator(ErrorTypeDefinition.class);
        uniqueFieldNamesValidator.validate(
                Stream.concat(definition.safeArgs().stream(), definition.unsafeArgs().stream())
                        .map(FieldDefinition::fieldName).collect(Collectors.toSet()));
    });

    private final ConjureValidator<ErrorTypeDefinition> validator;

    ErrorTypeDefinitionValidator(ConjureValidator<ErrorTypeDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ErrorTypeDefinition definition) {
        this.validator.validate(definition);
    }

}

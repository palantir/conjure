/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureValidator;

@com.google.errorprone.annotations.Immutable
public enum ErrorTypeDefinitionValidator implements ConjureValidator<ErrorTypeDefinition> {
    UNIQUE_ARG_NAMES(definition -> {
        UniqueFieldNamesValidator uniqueFieldNamesValidator = new UniqueFieldNamesValidator(ErrorTypeDefinition.class);
        uniqueFieldNamesValidator.validate(
                Sets.union(definition.safeArgs().keySet(), definition.unsafeArgs().keySet()));
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

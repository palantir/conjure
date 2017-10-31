/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureValidator;

public enum ObjectTypeDefinitionValidator implements ConjureValidator<ObjectTypeDefinition> {
    UNIQUE_FIELD_NAMES(definition -> {
        UniqueFieldNamesValidator uniqueFieldNamesValidator = new UniqueFieldNamesValidator(ObjectTypeDefinition.class);
        uniqueFieldNamesValidator.validate(definition.fields().keySet());
    });

    private final ConjureValidator<ObjectTypeDefinition> validator;

    ObjectTypeDefinitionValidator(ConjureValidator<ObjectTypeDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ObjectTypeDefinition definition) {
        this.validator.validate(definition);
    }
}

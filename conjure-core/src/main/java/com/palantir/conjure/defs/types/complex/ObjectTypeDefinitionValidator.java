/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.ObjectDefinition;
import java.util.stream.Collectors;

@com.google.errorprone.annotations.Immutable
public enum ObjectTypeDefinitionValidator implements ConjureValidator<ObjectDefinition> {
    UNIQUE_FIELD_NAMES(definition -> {
        UniqueFieldNamesValidator uniqueFieldNamesValidator = new UniqueFieldNamesValidator(ObjectDefinition.class);
        uniqueFieldNamesValidator.validate(definition.getFields().stream()
                .map(FieldDefinition::getFieldName).collect(Collectors.toSet()));
    });

    public static void validateAll(ObjectDefinition definition) {
        for (ObjectTypeDefinitionValidator validator : ObjectTypeDefinitionValidator.values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<ObjectDefinition> validator;

    ObjectTypeDefinitionValidator(ConjureValidator<ObjectDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ObjectDefinition definition) {
        this.validator.validate(definition);
    }
}

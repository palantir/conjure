/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.ObjectDefinition;
import java.util.stream.Collectors;

public final class ObjectDefinitionValidator {

    private ObjectDefinitionValidator() {}

    private static final UniqueFieldNamesValidator UNIQUE_FIELD_NAMES_VALIDATOR =
            new UniqueFieldNamesValidator(ObjectDefinition.class);

    public static void validate(ObjectDefinition definition) {
        UNIQUE_FIELD_NAMES_VALIDATOR.validate(
                definition.getFields().stream()
                        .map(FieldDefinition::getFieldName).collect(Collectors.toSet()));
    }
}

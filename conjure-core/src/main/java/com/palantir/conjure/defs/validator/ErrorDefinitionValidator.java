/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ErrorDefinitionValidator {

    private ErrorDefinitionValidator() {}

    private static final UniqueFieldNamesValidator UNIQUE_FIELD_NAMES_VALIDATOR =
            new UniqueFieldNamesValidator(ErrorDefinition.class);

    public static void validate(ErrorDefinition definition) {
        UNIQUE_FIELD_NAMES_VALIDATOR.validate(
                Stream.concat(definition.getSafeArgs().stream(), definition.getUnsafeArgs().stream())
                        .map(FieldDefinition::getFieldName).collect(Collectors.toSet()));
    }
}

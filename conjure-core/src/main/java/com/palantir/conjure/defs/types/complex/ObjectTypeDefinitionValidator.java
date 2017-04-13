/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.names.FieldName;
import java.util.Map;

public enum ObjectTypeDefinitionValidator implements ConjureValidator<ObjectTypeDefinition> {
    UNIQUE_FIELD_NAMES(new UniqueFieldNamesValidator());

    private final ConjureValidator<ObjectTypeDefinition> validator;

    ObjectTypeDefinitionValidator(ConjureValidator<ObjectTypeDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ObjectTypeDefinition definition) {
        this.validator.validate(definition);
    }

    /**
     * Verifies that type names are unique modulo normalization to {@link FieldName.Case#CAMEL_CASE_PATTERN
     * camel case}.
     */
    private static final class UniqueFieldNamesValidator implements ConjureValidator<ObjectTypeDefinition> {
        @Override
        public void validate(ObjectTypeDefinition definition) {
            Map<FieldName, FieldName> seenNormalizedToOriginal = Maps.newHashMap();
            for (FieldName fieldName : definition.fields().keySet()) {
                FieldName normalizedName = fieldName.toCase(FieldName.Case.LOWER_CAMEL_CASE);
                FieldName seenName = seenNormalizedToOriginal.get(normalizedName);
                Preconditions.checkArgument(seenName == null,
                        "%s must not contain duplicate field names (modulo case normalization): %s vs %s",
                        ObjectTypeDefinition.class.getSimpleName(),
                        fieldName.name(),
                        seenName == null ? "" : seenName.name());
                seenNormalizedToOriginal.put(normalizedName, fieldName);
            }
        }
    }
}

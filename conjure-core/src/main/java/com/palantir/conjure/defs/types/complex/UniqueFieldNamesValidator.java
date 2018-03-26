/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.names.FieldNameWrapper;
import com.palantir.conjure.defs.types.names.FieldNameWrapper.Case;
import com.palantir.conjure.spec.FieldName;
import java.util.Map;
import java.util.Set;

/**
 * Verifies that field names are unique modulo normalization to
 * {@link Case#LOWER_CAMEL_CASE lower camel case}.
 */

// TODO(qchen): field name case validator
@com.google.errorprone.annotations.Immutable
public final class UniqueFieldNamesValidator implements ConjureValidator<Set<FieldName>> {

    private final String classSimpleName;

    public UniqueFieldNamesValidator(Class<?> clazz) {
        classSimpleName = clazz.getSimpleName();
    }

    @Override
    public void validate(Set<FieldName> args) {
        Map<FieldName, FieldName> seenNormalizedToOriginal = Maps.newHashMap();
        for (FieldName argName : args) {
            FieldName normalizedName = FieldNameWrapper.toCase(argName, FieldNameWrapper.Case.LOWER_CAMEL_CASE);
            FieldName seenName = seenNormalizedToOriginal.get(normalizedName);
            Preconditions.checkArgument(seenName == null,
                    "%s must not contain duplicate field names (modulo case normalization): %s vs %s",
                    classSimpleName, argName.get(), seenName == null ? "" : seenName.get());
            seenNormalizedToOriginal.put(normalizedName, argName);
        }
    }

}

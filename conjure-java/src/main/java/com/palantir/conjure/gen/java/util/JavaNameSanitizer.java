/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.util;

import com.palantir.conjure.defs.types.names.FieldName;
import javax.lang.model.SourceVersion;

public final class JavaNameSanitizer {
    /**
     * Sanitizes the given {@link FieldName} for use as a java specifier.
     */
    public static String sanitize(FieldName fieldName) {
        String identifier = fieldName.toCase(FieldName.Case.LOWER_CAMEL_CASE).name();
        return sanitize(identifier);
    }

    private static String sanitize(String name) {
        return SourceVersion.isName(name) ? name : name + "_";
    }

    private JavaNameSanitizer() {}
}

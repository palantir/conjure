/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.ErrorNamespace;
import java.util.regex.Pattern;

public final class ErrorNamespaceValidator {

    private ErrorNamespaceValidator() {}

    private static final Pattern UPPER_CAMEL_CASE = Pattern.compile("(([A-Z][a-z0-9]+)+)");

    public static void validate(ErrorNamespace name) {
        Preconditions.checkArgument(UPPER_CAMEL_CASE.matcher(name.get()).matches(),
                "Namespace for errors must match pattern %s: %s", UPPER_CAMEL_CASE, name);
    }
}

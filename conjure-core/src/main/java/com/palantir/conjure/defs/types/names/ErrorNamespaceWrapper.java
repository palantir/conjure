/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.ErrorNamespace;
import java.util.regex.Pattern;

public final class ErrorNamespaceWrapper {

    private ErrorNamespaceWrapper() {}

    private static final Pattern UPPER_CAMEL_CASE = Pattern.compile("(([A-Z][a-z0-9]+)+)");

    public static void validate(String name) {
        Preconditions.checkArgument(UPPER_CAMEL_CASE.matcher(name).matches(),
                "Namespace for errors must match pattern %s: %s", UPPER_CAMEL_CASE, name);
    }

    public static ErrorNamespace errorNamespace(String name) {
        validate(name);
        return ErrorNamespace.of(name);
    }
}

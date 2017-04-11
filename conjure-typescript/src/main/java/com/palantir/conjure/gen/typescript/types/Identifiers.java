/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.google.common.base.CaseFormat;

public final class Identifiers {
    private Identifiers() {}

    public static String safeMemberName(String unsafe) {
        if (unsafe.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, unsafe);
        }
        return unsafe;
    }
}

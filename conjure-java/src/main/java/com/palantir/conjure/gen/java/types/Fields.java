/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.CaseFormat;

public final class Fields {

    private Fields() {}

    public static String toSafeFieldName(String unsafe) {
        if (unsafe.contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, unsafe);
        }
        return unsafe;
    }

}

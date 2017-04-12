/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.google.common.base.CaseFormat;
import com.palantir.conjure.defs.types.FieldName;

public final class Fields {

    private Fields() {}

    public static String toSafeFieldName(FieldName unsafe) {
        if (unsafe.name().contains("-")) {
            return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, unsafe.name());
        }
        return unsafe.name();
    }

}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import java.util.Optional;

public final class ObjectDefinitions {

    public static String getPackageName(Optional<String> packageName, Optional<String> defaultPackageName) {
        return packageName.orElseGet(() -> defaultPackageName.orElseThrow(
                () -> new IllegalStateException("Neither explicit nor default package configured")));
    }

    private ObjectDefinitions() {
    }

}

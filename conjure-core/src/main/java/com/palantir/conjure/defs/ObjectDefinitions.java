/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.palantir.conjure.defs.types.ConjurePackage;
import java.util.Optional;

public final class ObjectDefinitions {

    public static ConjurePackage getPackage(
            Optional<ConjurePackage> packageName, Optional<ConjurePackage> defaultPackageName) {
        return getPackage(packageName, defaultPackageName, "<unknown>");
    }

    public static ConjurePackage getPackage(
            Optional<ConjurePackage> packageName,
            Optional<ConjurePackage> defaultPackageName,
            String typeName) {
        return packageName.orElseGet(() -> defaultPackageName.orElseThrow(
                () -> new IllegalStateException(
                        "Neither explicit nor default package configured" + (typeName.isEmpty() ? ""
                                : " for type " + typeName))));
    }

    private ObjectDefinitions() {
    }

}

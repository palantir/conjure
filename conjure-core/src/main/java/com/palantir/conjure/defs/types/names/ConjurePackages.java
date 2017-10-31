/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import java.util.Optional;

public final class ConjurePackages {

    private ConjurePackages() {}

    public static ConjurePackage getPackage(
            Optional<ConjurePackage> packageName, Optional<ConjurePackage> defaultPackageName) {
        return getPackage(packageName, defaultPackageName, TypeName.UNKNOWN);
    }

    public static ConjurePackage getPackage(
            Optional<ConjurePackage> packageName,
            Optional<ConjurePackage> defaultPackageName,
            TypeName typeName) {
        return packageName.orElseGet(() -> defaultPackageName.orElseThrow(
                () -> new IllegalStateException(
                        "Neither explicit nor default package configured for type " + typeName.name())));
    }
}

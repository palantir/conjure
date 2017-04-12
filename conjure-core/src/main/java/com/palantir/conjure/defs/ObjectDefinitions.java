/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.palantir.conjure.defs.types.ConjurePackage;
import com.palantir.conjure.defs.types.TypeName;
import java.util.Optional;

public final class ObjectDefinitions {

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

    private ObjectDefinitions() {
    }

}

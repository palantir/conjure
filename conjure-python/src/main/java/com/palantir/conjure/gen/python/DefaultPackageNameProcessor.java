/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.ObjectDefinitions;
import java.util.Optional;

public final class DefaultPackageNameProcessor implements PackageNameProcessor {

    private final Optional<String> defaultPackage;

    public DefaultPackageNameProcessor(Optional<String> defaultPackage) {
        this.defaultPackage = defaultPackage;
    }

    @Override
    public String getPackageName(Optional<String> packageName) {
        return ObjectDefinitions.getPackageName(packageName, defaultPackage);
    }

}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import java.util.Optional;

public final class DefaultPackageNameProcessor implements PackageNameProcessor {

    private final String defaultPackage;

    public DefaultPackageNameProcessor(String defaultPackage) {
        this.defaultPackage = defaultPackage;
    }

    @Override
    public String getPackageName(Optional<String> packageName) {
        return packageName.orElse(defaultPackage);
    }

}

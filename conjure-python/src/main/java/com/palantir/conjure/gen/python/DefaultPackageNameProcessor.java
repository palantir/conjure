/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.ObjectDefinitions;
import com.palantir.conjure.defs.types.ConjurePackage;
import java.util.Optional;

public final class DefaultPackageNameProcessor implements PackageNameProcessor {

    private final Optional<ConjurePackage> defaultPackage;

    public DefaultPackageNameProcessor(Optional<ConjurePackage> defaultPackage) {
        this.defaultPackage = defaultPackage;
    }

    @Override
    public ConjurePackage getPackageName(Optional<ConjurePackage> conjurePackage) {
        return ObjectDefinitions.getPackage(conjurePackage, defaultPackage);
    }

}

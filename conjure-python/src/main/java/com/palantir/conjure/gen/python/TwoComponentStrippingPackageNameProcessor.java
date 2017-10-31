/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import java.util.List;
import java.util.Optional;

public final class TwoComponentStrippingPackageNameProcessor implements PackageNameProcessor {

    private final PackageNameProcessor delegate;

    public TwoComponentStrippingPackageNameProcessor(PackageNameProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public ConjurePackage getPackageName(Optional<ConjurePackage> conjurePackage) {
        ConjurePackage delegatePackageName = delegate.getPackageName(conjurePackage);
        List<String> components = delegatePackageName.components();

        if (components.size() > 2) {
            return ConjurePackage.of(components.subList(2, components.size()));
        } else {
            return delegatePackageName;
        }
    }

}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Optional;

public final class TwoComponentStrippingPackageNameProcessor implements PackageNameProcessor {

    private final PackageNameProcessor delegate;

    public TwoComponentStrippingPackageNameProcessor(PackageNameProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getPackageName(Optional<String> packageName) {
        String delegatePackageName = delegate.getPackageName(packageName);

        List<String> components = Splitter.on(".").splitToList(delegatePackageName);

        if (components.size() > 2) {
            return Joiner.on(".").join(components.subList(2, components.size()));
        } else {
            return delegatePackageName;
        }
    }

}

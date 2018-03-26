/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.types.names.ConjurePackageWrapper;
import java.util.List;

public final class TwoComponentStrippingPackageNameProcessor implements PackageNameProcessor {

    @Override
    public String getPackageName(String conjurePackage) {
        List<String> components = ConjurePackageWrapper.components(conjurePackage);

        if (components.size() > 2) {
            return ConjurePackageWrapper.conjurePackage(components.subList(2, components.size()));
        } else {
            return conjurePackage;
        }
    }

}

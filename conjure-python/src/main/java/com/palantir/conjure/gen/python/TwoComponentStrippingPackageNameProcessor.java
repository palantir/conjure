/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import java.util.List;

public final class TwoComponentStrippingPackageNameProcessor implements PackageNameProcessor {

    @Override
    public ConjurePackage getPackageName(ConjurePackage conjurePackage) {
        List<String> components = conjurePackage.components();

        if (components.size() > 2) {
            return ConjurePackage.of(components.subList(2, components.size()));
        } else {
            return conjurePackage;
        }
    }

}

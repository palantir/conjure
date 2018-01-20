/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.types.names.ConjurePackage;

/**
 * Post process package names.
 */
public interface PackageNameProcessor {
    ConjurePackage getPackageName(ConjurePackage conjurePackage);
}

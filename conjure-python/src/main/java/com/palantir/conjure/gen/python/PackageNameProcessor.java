/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.defs.types.ConjurePackage;
import java.util.Optional;

/**
 * Post process package names.
 */
public interface PackageNameProcessor {
    ConjurePackage getPackageName(Optional<ConjurePackage> conjurePackage);
}

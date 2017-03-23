/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import java.util.Optional;

/**
 * Post process package names.
 */
public interface PackageNameProcessor {

    String getPackageName(Optional<String> packageName);

}

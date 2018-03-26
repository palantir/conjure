/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

/**
 * Post process package names.
 */
public interface PackageNameProcessor {
    String getPackageName(String conjurePackage);
}

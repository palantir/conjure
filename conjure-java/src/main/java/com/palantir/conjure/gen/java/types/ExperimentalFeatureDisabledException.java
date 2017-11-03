/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import com.palantir.conjure.gen.java.ExperimentalFeatures;

/**
 * Consumers of conjure-java (e.g. conjure-gradle-plugin) can produce more specific error messages depending on how the
 * inputs are specified.
 */
public final class ExperimentalFeatureDisabledException extends RuntimeException {
    private final ExperimentalFeatures feature;

    public ExperimentalFeatureDisabledException(ExperimentalFeatures feature) {
        this.feature = feature;
    }

    public ExperimentalFeatures getFeature() {
        return feature;
    }
}

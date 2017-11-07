/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

@com.google.errorprone.annotations.Immutable
public interface ConjureValidator<T> {
    /**
     * Validates that the provided definition is valid according to Conjure rules. Throws an exception if the
     * provided definition is invalid.
     */
    void validate(T definition);
}

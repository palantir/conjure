/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

@com.google.errorprone.annotations.Immutable
public interface ConjureValidator<T> {
    /**
     * Validates that the provided definition is valid according to Conjure rules. Throws an exception if the
     * provided definition is invalid.
     */
    void validate(T definition);
}

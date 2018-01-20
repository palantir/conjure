/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

/**
 * A marker interface for the type system.
 */
public interface ConjureType {
    <T> T visit(ConjureTypeVisitor<T> visitor);
}

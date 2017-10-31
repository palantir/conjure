/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

public interface ImportStatementVisitor<T> {
    T visit(StandardImportStatement importStatement);

    T visit(StarImportStatement importStatement);
}

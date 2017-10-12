/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.gen.typescript.utils.ImportStatements;

public interface ImportStatement extends Comparable<ImportStatement>, Emittable {
    String filepathToImport();

    @Override
    default int compareTo(ImportStatement other) {
        return ImportStatements.COMPARATOR.compare(this, other);
    }

    <T> T accept(ImportStatementVisitor<T> visitor);
}

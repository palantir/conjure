/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.SimpleFileCollection;

public class ConjureExtension {
    private FileCollection conjureImports = new SimpleFileCollection();

    public final void setConjureImports(FileCollection files) {
        conjureImports = files;
    }

    public final void conjureImports(FileCollection files) {
        conjureImports = files;
    }

    public final FileCollection getConjureImports() {
        return conjureImports;
    }
}

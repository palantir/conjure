/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.google.common.collect.Sets;
import java.util.Set;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.SimpleFileCollection;

public class ConjureExtension {
    private FileCollection conjureImports = new SimpleFileCollection();
    private Set<String> experimentalFeatures = Sets.newHashSet();

    public final void setConjureImports(FileCollection files) {
        conjureImports = files;
    }

    public final void conjureImports(FileCollection files) {
        conjureImports = files;
    }

    public final FileCollection getConjureImports() {
        return conjureImports;
    }

    public final void experimentalFeature(String feature) {
        experimentalFeatures.add(feature);
    }

    public final Set<String> getExperimentalFeatures() {
        return experimentalFeatures;
    }
}

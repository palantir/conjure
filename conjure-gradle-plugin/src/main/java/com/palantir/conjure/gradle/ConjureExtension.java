/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.google.common.collect.Sets;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;

public class ConjureExtension {

    private final Project project;

    private ConfigurableFileCollection conjureImports;
    private Set<String> experimentalFeatures = Sets.newHashSet();

    public ConjureExtension(Project project) {
        this.project = project;
        conjureImports = project.files();
    }

    public final void setConjureImports(FileCollection files) {
        conjureImports = project.files(files);
    }

    public final void conjureImports(FileCollection files) {
        conjureImports.from(files);
    }

    public final ConfigurableFileCollection getConjureImports() {
        return conjureImports;
    }

    public final void experimentalFeature(String feature) {
        experimentalFeatures.add(feature);
    }

    public final Set<String> getExperimentalFeatures() {
        return experimentalFeatures;
    }
}

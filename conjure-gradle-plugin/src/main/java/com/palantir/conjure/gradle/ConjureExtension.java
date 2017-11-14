/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.google.common.collect.Sets;
import com.palantir.conjure.gen.python.types.PythonBeanGenerator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConjureExtension {
    private static final Logger log = LoggerFactory.getLogger(ConjureExtension.class);

    private final Project project;

    private ConfigurableFileCollection conjureImports;
    private final Set<String> experimentalFeatures = Sets.newHashSet();

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
        if (experimentalFeatures.contains("UnionTypes")) {
            log.warn("'UnionTypes' are no longer considered experimental and"
                    + " are now enabled by default - please remove this from your build.gradle.");
        }
        return experimentalFeatures.stream()
                .filter(string -> !string.equals("UnionTypes"))
                .collect(Collectors.toSet());
    }

    public final Set<com.palantir.conjure.gen.java.ExperimentalFeatures> getJavaExperimentalFeatures() {
        return getExperimentalFeatures().stream()
                .flatMap(maybeValueOf(com.palantir.conjure.gen.java.ExperimentalFeatures::valueOf))
                .collect(Collectors.toSet());
    }

    public final Set<PythonBeanGenerator.ExperimentalFeatures> getPythonExperimentalFeatures() {
        return getExperimentalFeatures().stream()
                .flatMap(maybeValueOf(PythonBeanGenerator.ExperimentalFeatures::valueOf))
                .collect(Collectors.toSet());
    }

    public final Set<com.palantir.conjure.gen.typescript.ExperimentalFeatures> getTypescriptExperimentalFeatures() {
        return getExperimentalFeatures().stream()
                .flatMap(maybeValueOf(com.palantir.conjure.gen.typescript.ExperimentalFeatures::valueOf))
                .collect(Collectors.toSet());
    }

    private static <T> Function<String, Stream<T>> maybeValueOf(Function<String, T> valueOf) {
        return string -> {
            try {
                T validValue = valueOf.apply(string);
                return Stream.of(validValue);
            } catch (IllegalArgumentException e) {
                return Stream.empty();
            }
        };
    }
}

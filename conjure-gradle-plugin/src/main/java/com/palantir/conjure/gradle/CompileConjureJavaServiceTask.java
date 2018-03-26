/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;

public class CompileConjureJavaServiceTask extends SourceTask {
    private File outputDirectory;
    private Function<Set<ExperimentalFeatures>, ServiceGenerator> serviceGeneratorFactory;
    private Supplier<Set<ExperimentalFeatures>> experimentalFeatures;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @OutputDirectory
    public final File getOutputDirectory() {
        return outputDirectory;
    }

    public final void setServiceGeneratorFactory(
            Function<Set<ExperimentalFeatures>, ServiceGenerator> serviceGeneratorFactory) {
        this.serviceGeneratorFactory = serviceGeneratorFactory;
    }

    public final void setExperimentalFeatures(Supplier<Set<ExperimentalFeatures>> experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

    @Input
    public final Set<ExperimentalFeatures> getExperimentalFeatures() {
        return experimentalFeatures.get();
    }

    @TaskAction
    public final void compileFiles() throws IOException {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);
        GFileUtils.cleanDirectory(outputDirectory);

        compileFiles(getSource().getFiles());
    }

    private void compileFiles(Collection<File> files) {
        ConjureDefinition conjure = Conjure.parse(files.stream().collect(Collectors.toList()));
        serviceGeneratorFactory.apply(experimentalFeatures.get()).emit(conjure, outputDirectory);
    }

}

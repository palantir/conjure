/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.python.ConjurePythonGenerator;
import com.palantir.conjure.gen.python.DefaultPythonFileWriter;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.python.types.DefaultBeanGenerator;
import com.palantir.conjure.gen.python.types.PythonBeanGenerator;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.GFileUtils;

public class CompileConjurePythonTask extends SourceTask {

    private File outputDirectory;

    private ClientGenerator clientGenerator;

    private Supplier<Set<PythonBeanGenerator.ExperimentalFeatures>> experimentalFeatures;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @OutputDirectory
    public final File getOutputDirectory() {
        return outputDirectory;
    }

    public final void setClientGenerator(ClientGenerator clientGenerator) {
        this.clientGenerator = clientGenerator;
    }

    public final void setExperimentalFeatures(
            Supplier<Set<PythonBeanGenerator.ExperimentalFeatures>> experimentalFeatures) {
        this.experimentalFeatures = experimentalFeatures;
    }

    @Input
    public final Set<PythonBeanGenerator.ExperimentalFeatures> getExperimentalFeatures() {
        return experimentalFeatures.get();
    }

    @TaskAction
    public final void compileFiles() {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);
        GFileUtils.cleanDirectory(outputDirectory);

        PythonBeanGenerator beanGenerator = new DefaultBeanGenerator(getExperimentalFeatures());

        compileFiles(beanGenerator, getSource().getFiles());
    }

    private void compileFiles(PythonBeanGenerator beanGenerator, Collection<File> files) {
        ConjureDefinition conjure = Conjure.parse(files.stream().collect(Collectors.toList()));
        ConjurePythonGenerator generator = new ConjurePythonGenerator(beanGenerator, clientGenerator);
        generator.write(conjure, new DefaultPythonFileWriter(outputDirectory.toPath()));
    }

}

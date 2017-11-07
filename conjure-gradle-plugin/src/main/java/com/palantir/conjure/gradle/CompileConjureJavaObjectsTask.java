/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.types.ExperimentalFeatureDisabledException;
import com.palantir.conjure.gen.java.types.ObjectGenerator;
import com.palantir.conjure.gen.java.types.TypeGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileConjureJavaObjectsTask extends SourceTask {

    @OutputDirectory
    private File outputDirectory;

    private Supplier<Set<ExperimentalFeatures>> experimentalFeatures;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
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

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();

        TypeGenerator generator = new ObjectGenerator(settings, experimentalFeatures.get());
        compileFiles(generator, ConjurePlugin.excludeExternalImports(getSource().getFiles()));

        GitIgnore.writeGitIgnore(outputDirectory, "*.java\n");
    }

    private void compileFiles(TypeGenerator generator, Collection<File> files) {
        files.forEach(f -> compileFile(generator, f.toPath()));
    }

    private void compileFile(TypeGenerator generator, Path path) {
        ConjureDefinition conjure = Conjure.parse(path.toFile());
        try {
            generator.emit(conjure, outputDirectory);
        } catch (ExperimentalFeatureDisabledException e) {
            String helpfulMessage = String.format("'%s' is an experimental feature. "
                            + "Add `conjure { experimentalFeature '%s' }` to your build.gradle to enable this.",
                    e.getFeature(), e.getFeature());
            throw new IllegalArgumentException(helpfulMessage, e);
        }
    }
}

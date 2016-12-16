/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure;

import static com.google.common.base.Preconditions.checkState;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.ConjureTypescriptClientGenerator;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileConjureTypescriptClientTask extends SourceTask {

    private File outputDirectory;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @OutputDirectory
    public final File getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public final void compileFiles() {
        compileFiles(getSource().getFiles());
    }

    private void compileFiles(Collection<File> files) {
        files.forEach(f -> compileFile(f.toPath()));
    }

    private void compileFile(Path path) {
        ConjureDefinition conjure = Conjure.parse(path.toFile());

        ConjureTypescriptClientGenerator generator = new ConjureTypescriptClientGenerator(new DefaultServiceGenerator(),
                new DefaultTypeGenerator());

        File outputDir = getOutputDirectory();
        checkState(outputDir.exists() || outputDir.mkdirs(), "Unable to make directory tree %s", outputDir);
        generator.emit(conjure, outputDir);
    }
}

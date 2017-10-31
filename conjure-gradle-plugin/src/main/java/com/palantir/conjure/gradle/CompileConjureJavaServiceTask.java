/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileConjureJavaServiceTask extends SourceTask {

    @OutputDirectory
    private File outputDirectory;

    @Input
    private Supplier<ServiceGenerator> serviceGenerator;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public final void setServiceGenerator(Supplier<ServiceGenerator> serviceGenerator) {
        this.serviceGenerator = serviceGenerator;
    }

    @TaskAction
    public final void compileFiles() throws IOException {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);

        compileFiles(ConjurePlugin.excludeExternalImports(getSource().getFiles()));
        // write a gitignore to prevent the generated files ending up in source control
        Files.write("*.java\n", new File(outputDirectory, ".gitignore"), StandardCharsets.UTF_8);
    }

    private void compileFiles(Collection<File> files) {
        files.forEach(f -> compileFile(f.toPath()));
    }

    private void compileFile(Path path) {
        ConjureDefinition conjure = Conjure.parse(path.toFile());
        serviceGenerator.get().emit(conjure, outputDirectory);
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.gen.python.ConjurePythonGenerator;
import com.palantir.conjure.gen.python.DefaultPythonFileWriter;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.python.types.BeanGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileConjurePythonTask extends SourceTask {

    @OutputDirectory
    private File outputDirectory;

    @Input
    private ClientGenerator clientGenerator;

    @Input
    private BeanGenerator beanGenerator;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public final void setClientGenerator(ClientGenerator clientGenerator) {
        this.clientGenerator = clientGenerator;
    }

    public final void setBeanGenerator(BeanGenerator beanGenerator) {
        this.beanGenerator = beanGenerator;
    }

    @TaskAction
    public final void compileFiles() throws IOException {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);

        Project project = getProject();
        File baseDir = new File(project.getBuildDir(), "conjure");

        compileFiles(getSource().getFiles(), baseDir.toPath());

        // write a gitignore to prevent the generated files ending up in source control
        Files.write("*.py\n", new File(outputDirectory, ".gitignore"), StandardCharsets.UTF_8);
    }

    private void compileFiles(Collection<File> files, Path baseDir) {
        files.forEach(f -> compileFile(f.toPath(), baseDir));
    }

    private void compileFile(Path path, Path baseDir) {
        ConjureDefinition conjure = Conjure.parse(path.toFile());
        ConjureImports conjureImports = Conjure.parseTypesFromConjureImports(conjure, baseDir);

        ConjurePythonGenerator generator = new ConjurePythonGenerator(beanGenerator, clientGenerator);

        generator.write(conjure, conjureImports, new DefaultPythonFileWriter(outputDirectory.toPath()));
    }

}

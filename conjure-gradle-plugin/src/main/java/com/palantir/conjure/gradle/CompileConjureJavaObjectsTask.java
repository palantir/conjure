/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.gen.java.types.TypeGenerator;
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

public class CompileConjureJavaObjectsTask extends SourceTask {

    @OutputDirectory
    private File outputDirectory;

    @Input
    private TypeGenerator typeGenerator;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public final void setTypeGenerator(TypeGenerator beanGenerator) {
        this.typeGenerator = beanGenerator;
    }

    @TaskAction
    public final void compileFiles() throws IOException {
        checkState(outputDirectory.exists() || outputDirectory.mkdirs(),
                "Unable to make directory tree %s", outputDirectory);

        Project project = getProject();

        File baseDir = new File(project.getBuildDir(), "conjure");

        compileFiles(getSource().getFiles(), baseDir.toPath());

        // write a gitignore to prevent the generated files ending up in source control
        Files.write("*.java\n", new File(outputDirectory, ".gitignore"), StandardCharsets.UTF_8);
    }

    private void compileFiles(Collection<File> files, Path baseDir) {
        files.forEach(f -> compileFile(f.toPath(), baseDir));
    }

    private void compileFile(Path path, Path baseDir) {
        ConjureDefinition conjure = Conjure.parse(path.toFile());
        ConjureImports imports = Conjure.parseImportsFromConjureDefinition(conjure, baseDir);
        typeGenerator.emit(conjure, imports, outputDirectory);
    }

}

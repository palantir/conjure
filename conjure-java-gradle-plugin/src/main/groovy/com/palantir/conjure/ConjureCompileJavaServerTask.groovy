/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure

import com.palantir.conjure.defs.Conjure
import com.palantir.conjure.defs.ConjureDefinition
import com.palantir.conjure.gen.java.Generators
import com.palantir.conjure.gen.java.Settings
import com.palantir.conjure.gen.java.TypeMapper.OptionalTypeStrategy
import java.nio.file.Path
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class ConjureCompileJavaServerTask extends SourceTask {
    Path outputDirectory

    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory.toFile()
    }

    @TaskAction
    void compileFiles() {
        compileFiles(getSource().getFiles())
    }

    void compileFiles(Collection<File> files) {
        files.each {file -> compileFile(file.toPath())}
    }

    def compileFile(Path path) {
        File outputDir = getOutputDirectory()
        outputDir.mkdirs()

        ConjureDefinition conjure = Conjure.parse(path.toFile());
        Generators.generateJerseyServices(conjure, Settings.of(OptionalTypeStrategy.Guava), outputDir);
    }
}

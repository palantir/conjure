/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure

import com.palantir.conjure.defs.Conjure
import com.palantir.conjure.defs.ConjureDefinition
import com.palantir.conjure.gen.java.ConjureJavaServiceAndTypeGenerator
import com.palantir.conjure.gen.java.Settings
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator
import com.palantir.conjure.gen.java.types.BeanGenerator
import com.palantir.conjure.gen.java.types.TypeMapper.OptionalTypeStrategy
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
        ConjureDefinition conjure = Conjure.parse(path.toFile());

        Settings settings = Settings.builder()
                .optionalTypeStrategy(OptionalTypeStrategy.Guava)
                .ignoreUnknownProperties(false)
                .build()
        ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                new JerseyServiceGenerator(settings),
                new BeanGenerator(settings))

        File outputDir = getOutputDirectory()
        outputDir.mkdirs()
        generator.emit(conjure, outputDir)
    }
}

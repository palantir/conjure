/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure;

import static com.google.common.base.Preconditions.checkState;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.ConjureJavaServiceAndTypeGenerator;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.types.BeanGenerator;
import com.palantir.conjure.gen.java.types.TypeMapper;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public class CompileConjureJavaServerTask extends SourceTask {

    private File outputDirectory;

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Input
    private TypeMapper.OptionalTypeStrategy optionalType;

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

        Settings settings = Settings.builder()
                .optionalTypeStrategy(optionalType)
                .ignoreUnknownProperties(true)
                .build();
        ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                new JerseyServiceGenerator(settings),
                new BeanGenerator(settings));

        File outputDir = getOutputDirectory();
        checkState(outputDir.exists() || outputDir.mkdirs(), "Unable to make directory tree %s", outputDir);
        generator.emit(conjure, outputDir);
    }

    public final void configure(TypeMapper.OptionalTypeStrategy optionalTypeStrategy) {
        this.optionalType = optionalTypeStrategy;
    }
}

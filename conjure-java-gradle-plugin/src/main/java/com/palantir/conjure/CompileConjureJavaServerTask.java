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
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
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

    @Input
    private FileCollection conjureImports;

    @OutputDirectory
    public final File getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public final void compileFiles() {
        Project project = getProject();
        File baseDir = project.file("build/conjure/");
        project.copy(copySpec ->
                copySpec.into(project.file(new File(baseDir, "external-imports")))
                        .from(conjureImports));
        project.copy(copySpec ->
                copySpec.into(project.file(baseDir))
                        .from(getSource()));

        compileFiles(getSource().getFiles(), baseDir.toPath());
    }

    private void compileFiles(Collection<File> files, Path baseDir) {
        files.forEach(f -> compileFile(f.toPath(), baseDir));
    }

    private void compileFile(Path path, Path baseDir) {
        ConjureDefinition conjure = Conjure.parse(path.toFile());

        Settings settings = Settings.builder()
                .optionalTypeStrategy(optionalType)
                .ignoreUnknownProperties(true)
                .build();
        ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                new JerseyServiceGenerator(settings),
                new BeanGenerator(settings), baseDir);

        File outputDir = getOutputDirectory();
        checkState(outputDir.exists() || outputDir.mkdirs(), "Unable to make directory tree %s", outputDir);
        generator.emit(conjure, outputDir);
    }

    public final void configure(TypeMapper.OptionalTypeStrategy optionalTypeStrategy, FileCollection imports) {
        this.optionalType = optionalTypeStrategy;
        this.conjureImports = imports;
    }
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class CompileTypeScriptJavaScriptTask extends ConventionTask {

    private File inputDirectory;
    private File outputDirectory;

    public final void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @InputDirectory
    public final File getInputDirectory() {
        return inputDirectory;
    }

    @OutputDirectory
    public final File getOutputDirectory() {
        return outputDirectory;
    }

    @TaskAction
    public final void compileFiles() {
        File workingDirectory = ConjurePublishPlugin.getTypescriptWorkingDirectory(getProject().getBuildDir());
        // install typescript compiler
        getProject().exec(execSpec -> {
            execSpec.commandLine("npm",
                    "install",
                    "--prefix",
                    ConjurePublishPlugin.getTypescriptWorkingDirectory(
                            getProject().getBuildDir()).getAbsolutePath(),
                    "typescript@2.1.4");
        });
        File tscExecutable = new File(workingDirectory, "node_modules/typescript/bin/tsc");

        // Construct a directory that we'll use to compile the typescript
        File typescriptWorkingDirectory =
                new File(ConjurePublishPlugin.getTypescriptWorkingDirectory(getProject().getBuildDir()),
                        "typescriptWorkingDirectory");

        // Write tsconfig.json
        File tsConfigFile = new File(typescriptWorkingDirectory, "tsconfig.json");
        ConjurePublishPlugin.copyFromResource("tsconfig.json", tsConfigFile);
        ConjurePublishPlugin.makeFile(tsConfigFile, ConjurePublishPlugin.readResource("tsconfig.json"));

        // Write e6-shim
        File es6ShimFile = new File(typescriptWorkingDirectory, "typings/es6-shim.d.ts");
        ConjurePublishPlugin.makeFile(es6ShimFile, ConjurePublishPlugin.readResource("es6-shim.d.ts"));

        // Write conjure fe lib typings
        File conjureFeLibTypings = new File(typescriptWorkingDirectory,
                "node_modules/@elements/conjure-fe-lib/index.d.ts");
        ConjurePublishPlugin.makeFile(conjureFeLibTypings,
                ConjurePublishPlugin.readResource("conjure-fe-lib_index.d.ts"));

        // Copy source
        ConjurePublishPlugin.copyDirectory(getInputDirectory(), new File(typescriptWorkingDirectory, "src"));

        // Compile typescript
        getProject().exec(execSpec -> {
            execSpec.commandLine(tscExecutable.getAbsolutePath(),
                    "--rootDir", ".");
            execSpec.setWorkingDir(typescriptWorkingDirectory);
        });

        // Copy build to output directory
        try {
            if (!getOutputDirectory().exists()) {
                FileUtils.forceMkdir(getOutputDirectory());
            }
            ConjurePublishPlugin.copyDirectory(new File(typescriptWorkingDirectory, "build"), getOutputDirectory());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

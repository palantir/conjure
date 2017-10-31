/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class CompileTypeScriptJavaScriptTask extends ConventionTask {

    @InputDirectory
    private File inputDirectory;

    @InputDirectory
    private File nodeModulesInputDirectory;

    @OutputDirectory
    private File outputDirectory;

    public final void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public final void setNodeModulesInputDirectory(File nodeModulesInputDirectory) {
        this.nodeModulesInputDirectory = nodeModulesInputDirectory;
    }

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @TaskAction
    public final void compileFiles() {
        // Construct a directory that we'll use to compile the typescript
        File typescriptWorkingDirectory = new File(getProject().getBuildDir(), "typeScriptWorkingDirectory");
        ConjurePublishPlugin.copyDirectory(inputDirectory, new File(typescriptWorkingDirectory, "src"));
        if (nodeModulesInputDirectory.exists()) {
            ConjurePublishPlugin.copyDirectory(nodeModulesInputDirectory,
                    new File(typescriptWorkingDirectory, "node_modules"));
        }

        // install typescript compiler
        getProject().exec(execSpec -> {
            execSpec.commandLine("npm",
                    "install",
                    "--prefix",
                    getProject().getBuildDir().getAbsolutePath(),
                    "typescript@2.1.4");
        });
        File tscExecutable = new File(getProject().getBuildDir(), "node_modules/typescript/bin/tsc");

        // Write tsconfig.json
        File tsConfigFile = new File(typescriptWorkingDirectory, "tsconfig.json");
        ConjurePublishPlugin.makeFile(tsConfigFile, ConjurePublishPlugin.readResource("tsconfig.json"));

        // Write e6-shim
        File es6ShimFile = new File(typescriptWorkingDirectory, "typings/es6-shim.d.ts");
        ConjurePublishPlugin.makeFile(es6ShimFile, ConjurePublishPlugin.readResource("es6-shim.d.ts"));

        // Write conjure fe lib typings
        File conjureFeLibTypings = new File(typescriptWorkingDirectory,
                "node_modules/@foundry/conjure-fe-lib/index.d.ts");
        ConjurePublishPlugin.makeFile(conjureFeLibTypings,
                ConjurePublishPlugin.readResource("conjure-fe-lib_index.d.ts"));

        // Compile typescript
        getProject().exec(execSpec -> {
            execSpec.commandLine(tscExecutable.getAbsolutePath(), "--rootDir", ".");
            // Cast to Object to maintain compatibility with Gradle 3.x
            execSpec.setWorkingDir((Object) typescriptWorkingDirectory);
        });

        // Copy build to output directory
        try {
            if (!outputDirectory.exists()) {
                FileUtils.forceMkdir(outputDirectory);
            }
            ConjurePublishPlugin.copyDirectory(new File(typescriptWorkingDirectory, "build/src"), outputDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // recopy package.json b/c tsc drops it
        for (File scopeDir : inputDirectory.listFiles()) {
            if (scopeDir.isDirectory()) {
                for (File packageDir : scopeDir.listFiles()) {
                    File packageJson = new File(packageDir, "package.json");
                    if (packageJson.exists()) {
                        try {
                            FileUtils.copyFile(packageJson,
                                    Paths.get(outputDirectory.getAbsolutePath(),
                                            scopeDir.getName(),
                                            packageDir.getName(),
                                            "package.json").toFile());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}

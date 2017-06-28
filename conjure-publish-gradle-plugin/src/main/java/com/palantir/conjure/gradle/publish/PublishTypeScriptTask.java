/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

public class PublishTypeScriptTask extends ConventionTask {

    @InputDirectory
    private File javaScriptInputDirectory;

    @InputDirectory
    private File npmrcInputDirectory;

    public final void setJavaScriptInputDirectory(File javaScriptInputDirectory) {
        this.javaScriptInputDirectory = javaScriptInputDirectory;
    }

    public final void setNpmrcInputDirectory(File npmrcInputDirectory) {
        this.npmrcInputDirectory = npmrcInputDirectory;
    }

    @TaskAction
    public final void publish() {
        File publishWorkingDirectory = new File(getProject().getBuildDir(), "publishWorkingDirectory");
        try {
            if (!publishWorkingDirectory.exists()) {
                FileUtils.forceMkdir(publishWorkingDirectory);
            }
            ConjurePublishPlugin.copyDirectory(javaScriptInputDirectory, publishWorkingDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (File scopeDir : publishWorkingDirectory.listFiles()) {
            if (scopeDir.isDirectory()) {
                for (File packageDir : scopeDir.listFiles()) {
                    // copy npm token for each scope
                    File npmrcFile = Paths.get(
                            npmrcInputDirectory.getAbsolutePath(),
                            scopeDir.getName(),
                            packageDir.getName(),
                            GenerateNpmrcTask.NPMRC_FILENAME).toFile();
                    if (npmrcFile.exists()) {
                        try {
                            FileUtils.copyFile(npmrcFile, new File(packageDir, GenerateNpmrcTask.NPMRC_FILENAME));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    getProject().exec(execSpec -> {
                        execSpec.setWorkingDir(packageDir);
                        execSpec.commandLine("npm", "publish", "./");
                    });
                }
            }
        }
    }

}

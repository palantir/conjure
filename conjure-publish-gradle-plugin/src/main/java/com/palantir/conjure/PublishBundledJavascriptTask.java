/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.TaskAction;

public class PublishBundledJavascriptTask extends ConventionTask {

    private File inputDirectory;

    public final void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    @InputDirectory
    public final File getInputDirectory() {
        return inputDirectory;
    }

    @TaskAction
    public final void bundleFiles() {
        File publishWorkingDirectory =
                new File(ConjurePublishPlugin.getTypescriptWorkingDirectory(getProject().getBuildDir()),
                        "publishWorkingDirectory");
        try {
            if (!publishWorkingDirectory.exists()) {
                FileUtils.forceMkdir(publishWorkingDirectory);
            }
            ConjurePublishPlugin.copyDirectory(getInputDirectory(), new File(publishWorkingDirectory, "dist"));
        } catch (IOException e) {
            throw new RuntimeException();
        }

        // Create publish script
        File publishFile = new File(publishWorkingDirectory, "scripts/publish.sh");
        ConjurePublishPlugin.copyFromResource("publish.sh", publishFile);
        publishFile.setExecutable(true);

        // Execute publish script
        getProject().exec(execSpec -> {
            execSpec.commandLine("./scripts/publish.sh");
            execSpec.setWorkingDir(publishWorkingDirectory);
        });
    }
}

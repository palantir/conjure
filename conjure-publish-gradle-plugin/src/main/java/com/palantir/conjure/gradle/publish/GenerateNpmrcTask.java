/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class GenerateNpmrcTask extends ConventionTask {

    @InputDirectory
    private File inputDirectory;

    @OutputDirectory
    private File outputDirectory;

    public final void setInputDirectory(File inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public final void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    private String getNpmRegistryUri() {
        return getProject().hasProperty("npmRegistryUri")
                ? getProject().property("npmRegistryUri").toString()
                : "https://artifactory.palantir.build/artifactory/api/npm/all-npm";
    }

    /**
     * Creates a .npmrc file for each package that will be published within the
     * initially empty outputDirectory.
     *
     * generateNpmrcOutput
     * └── @palantir
     *     ├── logreceiver-api
     *     │   └── .npmrc
     *     └── sls-spec-logging
     *         └── .npmrc
     */
    @TaskAction
    public final void createNpmrcFileForEachPackage() throws IOException {
        Files.createDirectories(outputDirectory.toPath());

        // Fetch npm token for each scope
        for (File scopeDir : inputDirectory.listFiles()) {

            Npmrc npmrc = Npmrc.fromArtifactoryCreds(
                    getNpmRegistryUri(),
                    Scope.of(scopeDir.getName()),
                    System.getenv("ARTIFACTORY_USERNAME"),
                    System.getenv("ARTIFACTORY_PASSWORD"));

            if (!scopeDir.isDirectory()) {
                continue;
            }

            for (File packageDir : scopeDir.listFiles()) {
                File fileToWrite = Paths.get(
                        outputDirectory.getAbsolutePath(),
                        scopeDir.getName(),
                        packageDir.getName(),
                        Npmrc.FILENAME).toFile();
                ConjurePublishPlugin.makeFile(fileToWrite, npmrc.get());
            }
        }
    }
}

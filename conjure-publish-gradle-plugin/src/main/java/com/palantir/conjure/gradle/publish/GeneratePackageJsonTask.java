/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class GeneratePackageJsonTask extends ConventionTask {

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
    public final void publishDirectory() {
        // Copy src
        ConjurePublishPlugin.copyDirectory(new File(getInputDirectory(), "src"), getOutputDirectory());

        // Add package.json for each package
        Handlebars handlebars = new Handlebars();
        try {
            Template template = handlebars.compileInline(ConjurePublishPlugin.readResource("package.json.hbs"));
            for (File scopeDir : getOutputDirectory().listFiles()) {
                for (File packageDir : scopeDir.listFiles()) {
                    createPackageJson(template, packageDir, scopeDir.getName(), packageDir.getName());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createPackageJson(Template template, File outputDir, String scopeName, String packageName)
            throws IOException {
        String packageJsonText = template.apply(
                ImmutableMap.of(
                        "scopeName", scopeName,
                        "packageName", packageName,
                        "version", getProject().getVersion()));
        ConjurePublishPlugin.makeFile(new File(outputDir, "package.json"), packageJsonText);
    }

}

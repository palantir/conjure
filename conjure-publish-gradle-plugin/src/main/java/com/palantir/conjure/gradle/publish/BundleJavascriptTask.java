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

public class BundleJavascriptTask extends ConventionTask {

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

        // Add package.json
        Handlebars handlebars = new Handlebars();
        try {
            Template template = handlebars.compileInline(ConjurePublishPlugin.readResource("package.json.hbs"));
            ConjurePublishPluginExtension extension = getProject().getExtensions()
                    .getByType(ConjurePublishPluginExtension.class);
            String packageJsonText = template.apply(
                    ImmutableMap.of(
                        "scopeName", extension.getScopeName(),
                        "projectName", getProject().getName(),
                        "version", getProject().getVersion()
                    )
            );
            ConjurePublishPlugin.makeFile(new File(getOutputDirectory(), "package.json"), packageJsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

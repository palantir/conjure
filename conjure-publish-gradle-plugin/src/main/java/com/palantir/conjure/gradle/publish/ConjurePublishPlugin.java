/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;

public class ConjurePublishPlugin implements Plugin<Project> {
    @Override
    public final void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        File workingDirectory = getTypescriptWorkingDirectory(project.getBuildDir());
        File compileTypeScriptOutput = new File(workingDirectory, "compileTypeScriptOutput");
        File bundleJavaScriptOutput = new File(workingDirectory, "bundleJavaScriptOutput");

        CompileTypeScriptJavaScriptTask compileTypescriptJavascriptTask = project.getTasks()
                .create("compileTypeScriptJavaScript", CompileTypeScriptJavaScriptTask.class);
        compileTypescriptJavascriptTask.setInputDirectory(project.file("src"));
        compileTypescriptJavascriptTask.setOutputDirectory(compileTypeScriptOutput);

        BundleJavascriptTask bundleJavascriptTask = project.getTasks()
                .create("bundleJavaScript", BundleJavascriptTask.class);
        bundleJavascriptTask.setInputDirectory(compileTypeScriptOutput);
        bundleJavascriptTask.setOutputDirectory(bundleJavaScriptOutput);

        PublishBundledJavascriptTask publishBundledJavascriptTask = project.getTasks()
                .create("publishBundledJavaScript", PublishBundledJavascriptTask.class);
        publishBundledJavascriptTask.setInputDirectory(bundleJavaScriptOutput);

        project.getExtensions().create("publishTypeScript", ConjurePublishPluginExtension.class, project);

        Task publishConjureTask = project.getTasks().create("publishTypeScript");

        bundleJavascriptTask.dependsOn(compileTypescriptJavascriptTask);
        publishBundledJavascriptTask.dependsOn(bundleJavascriptTask);

        publishConjureTask.dependsOn(publishBundledJavascriptTask);
    }

    public static File getTypescriptWorkingDirectory(File buildDirectory) {
        return new File(buildDirectory, "conjurePublish/typescriptClient");
    }

    public static String readResource(String path) {
        try {
            return Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeFile(File file, String content) {
        try {
            FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFromResource(String resource, File tsConfigFile) {
        makeFile(tsConfigFile, readResource(resource));
    }

    public static void copyDirectory(File source, File dest) {
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(File source, File dest) {
        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

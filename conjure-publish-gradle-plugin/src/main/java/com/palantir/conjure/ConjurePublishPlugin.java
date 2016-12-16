/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.JavaPlugin;

public class ConjurePublishPlugin implements Plugin<Project> {
    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public ConjurePublishPlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }

    @Override
    public final void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        // conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        File workingDirectory = getTypescriptWorkingDirectory(project.getBuildDir());
        File compileConjureOutput = new File(workingDirectory, "compileConjureOutput");
        File compileTypescriptOutput = new File(workingDirectory, "compileTypescriptOutput");
        File bundleJavascriptOutput = new File(workingDirectory, "bundleJavascriptOutput");

        CompileConjureTypescriptClientTask compileConjureTypescriptTask = project.getTasks()
                .create("compileConjureTypescriptClient", CompileConjureTypescriptClientTask.class);
        compileConjureTypescriptTask.setSource(conjureSourceSet);
        compileConjureTypescriptTask.setOutputDirectory(compileConjureOutput);

        CompileTypescriptJavascriptTask compileTypescriptJavascriptTask = project.getTasks()
                .create("compileTypescriptJavascript", CompileTypescriptJavascriptTask.class);
        compileTypescriptJavascriptTask.setInputDirectory(compileConjureOutput);
        compileTypescriptJavascriptTask.setOutputDirectory(compileTypescriptOutput);

        BundleJavascriptTask bundleJavascriptTask = project.getTasks()
                .create("bundleJavascript", BundleJavascriptTask.class);
        bundleJavascriptTask.setInputDirectory(compileTypescriptOutput);
        bundleJavascriptTask.setOutputDirectory(bundleJavascriptOutput);

        PublishBundledJavascriptTask publishBundledJavascriptTask = project.getTasks()
                .create("publishBundledJavascript", PublishBundledJavascriptTask.class);
        publishBundledJavascriptTask.setInputDirectory(bundleJavascriptOutput);

        Task publishConjureTask = project.getTasks().create("publishConjure");

        // task hierarchy
        //   publishBundledJavascript ->
        //   bundleJavascript ->
        //   compileTypescriptJavascript ->
        //   compileConjureTypescriptClient
        compileTypescriptJavascriptTask.dependsOn(compileConjureTypescriptTask);
        bundleJavascriptTask.dependsOn(compileTypescriptJavascriptTask);
        publishBundledJavascriptTask.dependsOn(bundleJavascriptTask);


        // task hierarchy
        //   publishConjure ->
        //   publishBundledJavascript
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

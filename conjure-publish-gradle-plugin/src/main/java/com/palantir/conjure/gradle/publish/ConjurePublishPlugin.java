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

public class ConjurePublishPlugin implements Plugin<Project> {

    @Override
    public final void apply(Project project) {
        File compileTypeScriptOutput = new File(project.getBuildDir(), "compileTypeScriptOutput");

        CompileTypeScriptJavaScriptTask compileTypescriptJavascriptTask = project.getTasks()
                .create("compileTypeScriptJavaScript", CompileTypeScriptJavaScriptTask.class);
        compileTypescriptJavascriptTask.setInputDirectory(project.file("src"));
        compileTypescriptJavascriptTask.setOutputDirectory(compileTypeScriptOutput);

        PublishTypeScriptTask publishTypeScriptTask = project.getTasks()
                .create("publishTypeScript", PublishTypeScriptTask.class);
        publishTypeScriptTask.setInputDirectory(compileTypeScriptOutput);
        publishTypeScriptTask.dependsOn(compileTypescriptJavascriptTask);

        project.afterEvaluate(p -> project.getTasks().maybeCreate("publish").dependsOn(publishTypeScriptTask));
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

    public static void copyDirectory(File source, File dest) {
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

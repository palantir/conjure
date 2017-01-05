/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure;

import java.util.Collections;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.FindBugs;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;

public class ConjureJavaPlugin implements Plugin<Project> {
    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public ConjureJavaPlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }

    @Override
    public final void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);

        // java generated source set
        SourceSet generatedSourceSet = project.getConvention().getPlugin(JavaPluginConvention.class)
                .getSourceSets().create("generated");

        // generated sources will not have resources, so disable the resource dir output to stop weird
        // eclipse classpath problems
        generatedSourceSet.getOutput().setResourcesDir(null);

        // conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        // compile depends on compileGenerated result
        project.getDependencies().add("compile", generatedSourceSet.getOutput());

        Delete cleanGeneratedCodeTask = project.getTasks().create("cleanGeneratedCode", Delete.class);
        conjureSourceSet.getSrcDirs().forEach(it -> cleanGeneratedCodeTask.getInputs().dir(it));
        generatedSourceSet.getAllSource().forEach(it -> cleanGeneratedCodeTask.getOutputs().dir(it));
        cleanGeneratedCodeTask.delete(generatedSourceSet.getAllSource().getSrcDirs());

        CompileConjureJavaServerTask compileConjureJavaServerTask = project.getTasks()
                .create("compileConjureJavaServer", CompileConjureJavaServerTask.class);
        compileConjureJavaServerTask.setSource(conjureSourceSet);
        compileConjureJavaServerTask.setOutputDirectory(project.file("src/generated/java"));

        Task compileGeneratedJavaTask = project.getTasks()
                .getByName(generatedSourceSet.getCompileJavaTaskName());

        // task hierarchy (compileJava -> compileGeneratedJava -> compileConjure* -> cleanGenerated)
        compileConjureJavaServerTask.dependsOn(cleanGeneratedCodeTask);
        compileGeneratedJavaTask.dependsOn(compileConjureJavaServerTask);
        project.getTasks().getByName("compileJava").dependsOn(compileGeneratedJavaTask);

        // dependency cleanup
        project.getConfigurations().getByName("compile").extendsFrom(
                project.getConfigurations().getByName(generatedSourceSet.getCompileConfigurationName()));

        // make sure generated code gets included in the jar
        Jar jar = (Jar) project.getTasks().getByName("jar");
        jar.from(generatedSourceSet.getOutput());
        jar.dependsOn(compileGeneratedJavaTask);

        try {
            Jar sourceJar = (Jar) project.getTasks().getByName("sourceJar");
            sourceJar.from(generatedSourceSet.getAllSource());
            sourceJar.dependsOn(compileConjureJavaServerTask);
        } catch (UnknownTaskException e) {
            // meh
        }

        // fix source dirs for idea
        project.getPlugins().withType(IdeaPlugin.class, plugin -> {
            IdeaModel idea = project.getExtensions().getByType(IdeaModel.class);
            idea.getModule().getSourceDirs().addAll(generatedSourceSet.getAllSource().getSrcDirs());
        });

        // exclude checkstyle + findbugs
        TaskCollection<Checkstyle> checkstyles = project.getTasks().withType(Checkstyle.class);
        if (!checkstyles.isEmpty()) {
            checkstyles.getByName(generatedSourceSet.getTaskName("checkstyle", "")).setEnabled(false);
        }
        TaskCollection<FindBugs> findbugs = project.getTasks().withType(FindBugs.class);
        if (!findbugs.isEmpty()) {
            findbugs.getByName(generatedSourceSet.getTaskName("findbugs", "")).setEnabled(false);
        }
    }
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.java.types.BeanGenerator;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import java.util.Collections;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;

public class ConjurePlugin implements Plugin<Project> {
    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public ConjurePlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }

    @Override
    public final void apply(Project project) {
        ConjureExtension extension = project.getExtensions().create("conjure", ConjureExtension.class);

        project.evaluationDependsOnChildren();

        // conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        ProcessConjureImportsTask processConjureImports = project.getTasks().create(
                "processConjureImports", ProcessConjureImportsTask.class);
        processConjureImports.setSource(conjureSourceSet);
        processConjureImports.setConjureImports(() -> extension.getConjureImports());

        Task conjureTask = project.getTasks().create("compileConjure", DefaultTask.class);

        applyDependencyForIdeTasks(project, conjureTask);

        Project retrofitClientProject = project.project(project.getName() + "-retrofit-client", (subproj) -> {
            subproj.getPluginManager().apply(JavaPlugin.class);
            applyDependencyForIdeTasks(subproj, conjureTask);
            subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            subproj.getDependencies().add("compile", "com.squareup.retrofit2:retrofit");
            subproj.getTasks().getByName("compileJava").dependsOn(conjureTask);
            addGeneratedToMainSourceSet(subproj);
        });

        Project jerseyClientProject = project.project(project.getName() + "-jersey-client", (subproj) -> {
            subproj.getPluginManager().apply(JavaPlugin.class);
            applyDependencyForIdeTasks(subproj, conjureTask);
            subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            subproj.getDependencies().add("compile", "javax.ws.rs:javax.ws.rs-api");
            subproj.getTasks().getByName("compileJava").dependsOn(conjureTask);
            addGeneratedToMainSourceSet(subproj);
        });

        Project jerseyServerProject = project.project(project.getName() + "-jersey-server", (subproj) -> {
            subproj.getPluginManager().apply(JavaPlugin.class);
            applyDependencyForIdeTasks(subproj, conjureTask);
            subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            subproj.getDependencies().add("compile", "javax.ws.rs:javax.ws.rs-api");
            subproj.getTasks().getByName("compileJava").dependsOn(conjureTask);
            addGeneratedToMainSourceSet(subproj);
        });

        Project typescriptClientProject = project.project(project.getName() + "-typescript-client", (subproj) -> {
            applyDependencyForIdeTasks(subproj, conjureTask);
        });

        CompileConjureJavaTask compileConjureJerseyServerTask =
                createConjureJerseyServerTask(project, jerseyServerProject, conjureSourceSet, processConjureImports);

        CompileConjureJavaTask compileConjureJerseyClientTask =
                createConjureJerseyClientTask(project, jerseyClientProject, conjureSourceSet, processConjureImports);

        CompileConjureJavaTask compileConjureRetrofitClientTask =
                createConjureRetrofitClientTask(project, retrofitClientProject, conjureSourceSet,
                        processConjureImports);

        CompileConjureTypeScriptTask compileConjureTypeScriptClientTask =
                createConjureTypeScriptClientTask(project, typescriptClientProject, conjureSourceSet,
                        processConjureImports);

        conjureTask.dependsOn(
                compileConjureJerseyServerTask,
                compileConjureJerseyClientTask,
                compileConjureRetrofitClientTask,
                compileConjureTypeScriptClientTask);
    }

    private void addGeneratedToMainSourceSet(Project subproj) {
        JavaPluginConvention javaPlugin = subproj.getConvention().findPlugin(JavaPluginConvention.class);
        javaPlugin.getSourceSets().getByName("main").getJava().setSrcDirs(subproj.files("src/generated/java"));
    }

    private void applyDependencyForIdeTasks(Project project, Task conjureTask) {
        project.getPlugins().withType(IdeaPlugin.class, plugin -> {
            Task task = project.getTasks().findByName("idea");
            if (task != null) {
                task.dependsOn(conjureTask);
            }

            plugin.getModel().getModule().getSourceDirs().add(project.file("src/generated/java"));
            plugin.getModel().getModule().getGeneratedSourceDirs().add(project.file("src/generated/java"));
        });
        project.getPlugins().withType(EclipsePlugin.class, plugin -> {
            Task task = project.getTasks().findByName("eclipse");
            if (task != null) {
                task.dependsOn(conjureTask);
            }
        });
    }

    private CompileConjureJavaTask createConjureJerseyServerTask(Project conjureProject,
            Project destinationProject, SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = conjureProject.getTasks()
                .create("compileConjureJerseyServer", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> destinationProject.file("src/generated/java"));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(false)
                .supportUnknownEnumValues(false)
                .build();
        task.setServiceGenerator(new JerseyServiceGenerator());
        task.setTypeGenerator(new BeanGenerator(settings));
        return task;
    }

    private CompileConjureJavaTask createConjureJerseyClientTask(Project conjureProject,
            Project destinationProject, SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = conjureProject.getTasks()
                .create("compileConjureJerseyClient", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> destinationProject.file("src/generated/java"));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();
        task.setServiceGenerator(new JerseyServiceGenerator());
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }

    private CompileConjureJavaTask createConjureRetrofitClientTask(Project conjureProject,
            Project destinationProject, SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = conjureProject.getTasks()
                .create("compileConjureRetrofitClient", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> destinationProject.file("src/generated/java"));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();
        task.setServiceGenerator(new Retrofit2ServiceGenerator());
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }

    private CompileConjureTypeScriptTask createConjureTypeScriptClientTask(Project conjureProject,
            Project destinationProject, SourceDirectorySet conjureSourceSet,
            ProcessConjureImportsTask processConjureImports) {
        CompileConjureTypeScriptTask task = conjureProject.getTasks()
                .create("compileConjureTypeScriptClient", CompileConjureTypeScriptTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> destinationProject.file("src"));

        task.setServiceGenerator(new DefaultServiceGenerator());
        task.setTypeGenerator(new DefaultTypeGenerator());

        return task;
    }
}

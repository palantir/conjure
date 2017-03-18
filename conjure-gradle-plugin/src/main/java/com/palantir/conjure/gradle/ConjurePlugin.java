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
import nebula.plugin.publishing.maven.MavenBasePublishPlugin;
import nebula.plugin.publishing.maven.MavenResolvedDependenciesPlugin;
import nebula.plugin.publishing.publications.JavadocJarPlugin;
import nebula.plugin.publishing.publications.SourceJarPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
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

        CompileConjureJavaTask compileConjureJerseyServerTask =
                createConjureJerseyServerTask(project, conjureSourceSet, processConjureImports);

        CompileConjureJavaTask compileConjureJerseyClientTask =
                createConjureJerseyClientTask(project, conjureSourceSet, processConjureImports);

        CompileConjureJavaTask compileConjureRetrofitClientTask =
                createConjureRetrofitClientTask(project, conjureSourceSet, processConjureImports);

        CompileConjureTypeScriptTask compileConjureTypeScriptClientTask =
                createConjureTypeScriptClientTask(project, conjureSourceSet, processConjureImports);

        Task conjureTask = project.getTasks().create("compileConjure", DefaultTask.class);
        conjureTask.dependsOn(
                compileConjureJerseyServerTask,
                compileConjureJerseyClientTask,
                compileConjureRetrofitClientTask,
                compileConjureTypeScriptClientTask);

        applyDependencyForIdeTasks(project, conjureTask);

        project.project("retrofit-client", (subproj) -> {
            configureJavaSubprojectPlugins(subproj);
            applyDependencyForIdeTasks(subproj, conjureTask);
            subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            subproj.getDependencies().add("compile", "com.squareup.retrofit2:retrofit");
            subproj.getTasks().getByName("compileJava").dependsOn(conjureTask);
            addGeneratedToMainSourceSet(subproj);
            configurePublicationName(subproj, project.getName() + "-retrofit-client");
        });

        project.project("jersey-client", (subproj) -> {
            configureJavaSubprojectPlugins(subproj);
            applyDependencyForIdeTasks(subproj, conjureTask);
            subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            subproj.getDependencies().add("compile", "javax.ws.rs:javax.ws.rs-api");
            subproj.getTasks().getByName("compileJava").dependsOn(conjureTask);
            addGeneratedToMainSourceSet(subproj);
            configurePublicationName(subproj, project.getName() + "-jersey-client");
        });

        project.project("jersey-server", (subproj) -> {
            configureJavaSubprojectPlugins(subproj);
            applyDependencyForIdeTasks(subproj, conjureTask);
            subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            subproj.getDependencies().add("compile", "javax.ws.rs:javax.ws.rs-api");
            subproj.getTasks().getByName("compileJava").dependsOn(conjureTask);
            addGeneratedToMainSourceSet(subproj);
            configurePublicationName(subproj, project.getName() + "-jersey-server");
        });

        project.project("typescript-client", (subproj) -> {
            applyDependencyForIdeTasks(subproj, conjureTask);
        });
    }

    private void configurePublicationName(Project subproj, String name) {
        PublishingExtension publishing = subproj.getExtensions().getByType(PublishingExtension.class);
        MavenPublication publication = (MavenPublication) publishing.getPublications().getByName("nebula");
        publication.setArtifactId(name);
    }

    private void addGeneratedToMainSourceSet(Project subproj) {
        JavaPluginConvention javaPlugin = subproj.getConvention().findPlugin(JavaPluginConvention.class);
        javaPlugin.getSourceSets().getByName("main").getJava().setSrcDirs(subproj.files("src/generated/java"));
    }

    private void configureJavaSubprojectPlugins(Project subproj) {
        subproj.getPluginManager().apply(JavaPlugin.class);
        subproj.getPluginManager().apply(MavenPublishPlugin.class);
        subproj.getPluginManager().apply(MavenBasePublishPlugin.class);
        subproj.getPluginManager().apply(MavenResolvedDependenciesPlugin.class);
        subproj.getPluginManager().apply(JavadocJarPlugin.class);
        subproj.getPluginManager().apply(SourceJarPlugin.class);
    }

    private void applyDependencyForIdeTasks(Project project, Task conjureTask) {
        project.getPlugins().withType(IdeaPlugin.class, plugin -> {
            Task task = project.getTasks().findByName("idea");
            if (task != null) {
                task.dependsOn(conjureTask);
            }
        });
        project.getPlugins().withType(EclipsePlugin.class, plugin -> {
            Task task = project.getTasks().findByName("eclipse");
            if (task != null) {
                task.dependsOn(conjureTask);
            }
        });
    }

    private CompileConjureJavaTask createConjureJerseyServerTask(Project project,
            SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = project.getTasks()
                .create("compileConjureJerseyServer", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> project.file("jersey-server/src/generated/java"));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(false)
                .supportUnknownEnumValues(false)
                .build();
        task.setServiceGenerator(new JerseyServiceGenerator());
        task.setTypeGenerator(new BeanGenerator(settings));
        return task;
    }

    private CompileConjureJavaTask createConjureJerseyClientTask(Project project,
            SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = project.getTasks()
                .create("compileConjureJerseyClient", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> project.file("jersey-client/src/generated/java"));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();
        task.setServiceGenerator(new JerseyServiceGenerator());
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }

    private CompileConjureJavaTask createConjureRetrofitClientTask(Project project,
            SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = project.getTasks()
                .create("compileConjureRetrofitClient", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> project.file("retrofit-client/src/generated/java"));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();
        task.setServiceGenerator(new Retrofit2ServiceGenerator());
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }

    private CompileConjureTypeScriptTask createConjureTypeScriptClientTask(Project project,
            SourceDirectorySet conjureSourceSet, ProcessConjureImportsTask processConjureImports) {
        CompileConjureTypeScriptTask task = project.getTasks()
                .create("compileConjureTypeScriptClient", CompileConjureTypeScriptTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);
        task.setOutputDirectory(() -> project.file("typescript-client/src"));

        task.setServiceGenerator(new DefaultServiceGenerator());
        task.setTypeGenerator(new DefaultTypeGenerator());

        return task;
    }
}

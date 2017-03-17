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

        // conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        ProcessConjureImportsTask processConjureImports = project.getTasks().create(
                "processConjureImports", ProcessConjureImportsTask.class);
        processConjureImports.setSource(conjureSourceSet);
        processConjureImports.setConjureImports(() -> extension.getConjureImports());

        CompileConjureJavaTask compileConjureJerseyServerTask =
                createConjureJerseyServerTask(project, extension, conjureSourceSet, processConjureImports);

        CompileConjureJavaTask compileConjureJerseyClientTask =
                createConjureJerseyClientTask(project, extension, conjureSourceSet, processConjureImports);

        CompileConjureJavaTask compileConjureRetrofitClientTask =
                createConjureRetrofitClientTask(project, extension, conjureSourceSet, processConjureImports);

        CompileConjureTypeScriptTask compileConjureTypeScriptClientTask =
                createConjureTypeScriptClientTask(project, extension, conjureSourceSet, processConjureImports);

        Task conjureTask = project.getTasks().create("compileConjure", DefaultTask.class);
        conjureTask.dependsOn(
                compileConjureJerseyServerTask,
                compileConjureJerseyClientTask,
                compileConjureRetrofitClientTask,
                compileConjureTypeScriptClientTask);

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

    private CompileConjureJavaTask createConjureJerseyServerTask(Project project, ConjureExtension extension,
            SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = project.getTasks()
                .create("compileConjureJerseyServer", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);

        task.onlyIf(t -> extension.getJerseyServer().isPresent());
        task.setOutputDirectory(() -> extension.getJerseyServer()
                .map(conf -> conf.getOutput())
                .orElse(null));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(false)
                .supportUnknownEnumValues(false)
                .build();
        task.setServiceGenerator(new JerseyServiceGenerator(settings));
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }

    private CompileConjureJavaTask createConjureJerseyClientTask(Project project, ConjureExtension extension,
            SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = project.getTasks()
                .create("compileConjureJerseyClient", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);

        task.onlyIf(t -> extension.getJerseyClient().isPresent());
        task.setOutputDirectory(() -> extension.getJerseyClient()
                .map(conf -> conf.getOutput())
                .orElse(null));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();
        task.setServiceGenerator(new JerseyServiceGenerator(settings));
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }

    private CompileConjureJavaTask createConjureRetrofitClientTask(Project project, ConjureExtension extension,
            SourceDirectorySet conjureSourceSet, Task processConjureImports) {
        CompileConjureJavaTask task = project.getTasks()
                .create("compileConjureRetrofitClient", CompileConjureJavaTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);

        task.onlyIf(t -> extension.getRetrofitClient().isPresent());
        task.setOutputDirectory(() -> extension.getRetrofitClient()
                .map(conf -> conf.getOutput())
                .orElse(null));

        Settings settings = Settings.builder()
                .ignoreUnknownProperties(true)
                .supportUnknownEnumValues(true)
                .build();
        task.setServiceGenerator(new Retrofit2ServiceGenerator(settings));
        task.setTypeGenerator(new BeanGenerator(settings));

        return task;
    }


    private CompileConjureTypeScriptTask createConjureTypeScriptClientTask(Project project, ConjureExtension extension,
            SourceDirectorySet conjureSourceSet, ProcessConjureImportsTask processConjureImports) {
        CompileConjureTypeScriptTask task = project.getTasks()
                .create("compileConjureTypeScriptClient", CompileConjureTypeScriptTask.class);
        task.setSource(conjureSourceSet);
        task.dependsOn(processConjureImports);

        task.onlyIf(t -> extension.getTypeScriptClient().isPresent());
        task.setOutputDirectory(() -> extension.getTypeScriptClient()
                .map(conf -> conf.getOutput())
                .orElse(null));

        task.setServiceGenerator(new DefaultServiceGenerator());
        task.setTypeGenerator(new DefaultTypeGenerator());

        return task;
    }
}

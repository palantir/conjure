/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.python.types.PythonBeanGenerator;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;

public class ConjurePlugin implements Plugin<Project> {

    private static final String EXTERNAL_IMPORTS_DIRNAME = "external-imports";

    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public ConjurePlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }

    @Override
    @SuppressWarnings("checkstyle:methodlength")
    public final void apply(Project project) {
        ConjureExtension extension = project.getExtensions().create("conjure", ConjureExtension.class, project);

        // Conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        // Copy conjure imports into build directory
        File buildDir = new File(project.getBuildDir(), "conjure");
        Task processConjureImports = project.getTasks().create("processConjureImports", DefaultTask.class);
        processConjureImports.doLast(task ->
                project.copy(copySpec -> copySpec.into(project.file(new File(buildDir, EXTERNAL_IMPORTS_DIRNAME)))
                        .from(extension.getConjureImports())));

        // Copy conjure sources into build directory
        Copy copyConjureSourcesTask = project.getTasks().create("copyConjureSourcesIntoBuild", Copy.class);
        copyConjureSourcesTask.into(project.file(buildDir)).from(conjureSourceSet);

        // Set up conjure compile task
        Task conjureTask = project.getTasks().create("compileConjure", DefaultTask.class);
        applyDependencyForIdeTasks(project, conjureTask);

        final String objectsProjectName = project.getName() + "-objects";
        final String retrofitProjectName = project.getName() + "-retrofit";
        final String jerseyProjectName = project.getName() + "-jersey";
        final String typescriptProjectName = project.getName() + "-typescript";
        final String pythonProjectName = project.getName() + "-python";

        final Supplier<Set<ExperimentalFeatures>> experimentalFeaturesSupplier =
                () -> extension.getExperimentalFeatures()
                        .stream()
                        .map(ExperimentalFeatures::valueOf)
                        .collect(Collectors.toSet());

        final Project objectsProject;
        if (project.findProject(objectsProjectName) != null) {
            objectsProject = project.project(objectsProjectName, (subproj) -> {
                subproj.getPluginManager().apply(JavaPlugin.class);

                addGeneratedToMainSourceSet(subproj);
                project.getTasks().create("compileConjureObjects",
                        CompileConjureJavaObjectsTask.class,
                        (task) -> {
                            task.dependsOn(processConjureImports);
                            task.setSource(copyConjureSourcesTask);
                            task.setOutputDirectory(subproj.file("src/generated/java"));
                            task.setExperimentalFeatures(experimentalFeaturesSupplier);
                            conjureTask.dependsOn(task);
                            subproj.getTasks().getByName("compileJava").dependsOn(task);
                            applyDependencyForIdeTasks(subproj, task);
                        });

                subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            });
        } else {
            objectsProject = null;
        }

        if (project.findProject(retrofitProjectName) != null) {
            if (objectsProject == null) {
                throw new IllegalStateException(
                        String.format("Cannot enable '%s' without '%s'", retrofitProjectName, objectsProjectName));
            }
            project.project(retrofitProjectName, (subproj) -> {
                subproj.getPluginManager().apply(JavaPlugin.class);

                addGeneratedToMainSourceSet(subproj);

                project.getTasks().create("compileConjureRetrofit",
                        CompileConjureJavaServiceTask.class,
                        (task) -> {
                            task.dependsOn(processConjureImports);
                            task.setSource(copyConjureSourcesTask);
                            task.setOutputDirectory(subproj.file("src/generated/java"));
                            task.setServiceGenerator(
                                    () -> new Retrofit2ServiceGenerator(experimentalFeaturesSupplier.get()));
                            conjureTask.dependsOn(task);
                            subproj.getTasks().getByName("compileJava").dependsOn(task);
                            applyDependencyForIdeTasks(subproj, task);
                        });

                subproj.getDependencies().add("compile", objectsProject);
                subproj.getDependencies().add("compile", "com.squareup.retrofit2:retrofit");
            });
        }

        if (project.findProject(jerseyProjectName) != null) {
            if (objectsProject == null) {
                throw new IllegalStateException(
                        String.format("Cannot enable '%s' without '%s'", jerseyProjectName, objectsProjectName));
            }
            project.project(project.getName() + "-jersey", (subproj) -> {
                subproj.getPluginManager().apply(JavaPlugin.class);

                addGeneratedToMainSourceSet(subproj);

                project.getTasks().create("compileConjureJersey",
                        CompileConjureJavaServiceTask.class,
                        (task) -> {
                            task.dependsOn(processConjureImports);
                            task.setSource(copyConjureSourcesTask);
                            task.setOutputDirectory(subproj.file("src/generated/java"));
                            task.setServiceGenerator(JerseyServiceGenerator::new);
                            conjureTask.dependsOn(task);
                            subproj.getTasks().getByName("compileJava").dependsOn(task);
                            applyDependencyForIdeTasks(subproj, task);
                        });

                subproj.getDependencies().add("compile", objectsProject);
                subproj.getDependencies().add("compile", "javax.ws.rs:javax.ws.rs-api");
            });
        }

        if (project.findProject(typescriptProjectName) != null) {
            project.project(typescriptProjectName, (subproj) -> {
                applyDependencyForIdeTasks(subproj, conjureTask);

                project.getTasks().create("compileConjureTypeScript",
                        CompileConjureTypeScriptTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            task.dependsOn(processConjureImports);
                            task.setOutputDirectory(subproj.file("src"));
                            task.setNodeModulesOutputDirectory(new File(subproj.getBuildDir(), "node_modules"));
                            task.setServiceGenerator(new DefaultServiceGenerator());
                            task.setTypeGenerator(new DefaultTypeGenerator());
                            conjureTask.dependsOn(task);
                        });
            });
        }

        if (project.findProject(pythonProjectName) != null) {
            project.project(pythonProjectName, (subproj) -> {
                applyDependencyForIdeTasks(subproj, conjureTask);

                project.getTasks().create("compileConjurePython",
                        CompileConjurePythonTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            task.dependsOn(processConjureImports);
                            task.setOutputDirectory(subproj.file("python"));
                            task.setClientGenerator(new ClientGenerator());
                            task.setExperimentalFeatures(new Supplier<Set<PythonBeanGenerator.ExperimentalFeatures>>() {
                                @Override
                                public Set<PythonBeanGenerator.ExperimentalFeatures> get() {
                                    return extension.getExperimentalFeatures()
                                            .stream()
                                            .map(PythonBeanGenerator.ExperimentalFeatures::valueOf)
                                            .collect(Collectors.toSet());
                                }
                            });
                            conjureTask.dependsOn(task);
                        });
            });
        }
    }

    static Set<File> excludeExternalImports(Set<File> files) {
        return files.stream()
                .filter(f -> !Objects.equals(f.getParentFile().getName(), EXTERNAL_IMPORTS_DIRNAME))
                .collect(Collectors.toSet());
    }

    private void addGeneratedToMainSourceSet(Project subproj) {
        JavaPluginConvention javaPlugin = subproj.getConvention().findPlugin(JavaPluginConvention.class);
        javaPlugin.getSourceSets().getByName("main").getJava().srcDir(subproj.files("src/generated/java"));
    }

    private void applyDependencyForIdeTasks(Project project, Task conjureTask) {
        project.getPlugins().withType(IdeaPlugin.class, plugin -> {
            Task task = project.getTasks().findByName("ideaModule");
            if (task != null) {
                task.dependsOn(conjureTask);
            }

            plugin.getModel().getModule().getSourceDirs().add(project.file("src/generated/java"));
            plugin.getModel().getModule().getGeneratedSourceDirs().add(project.file("src/generated/java"));
        });
        project.getPlugins().withType(EclipsePlugin.class, plugin -> {
            Task task = project.getTasks().findByName("eclipseClasspath");
            if (task != null) {
                task.dependsOn(conjureTask);
            }
        });
    }
}

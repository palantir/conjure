/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.typescript.errors.DefaultErrorGenerator;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.SourceDirectorySetFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.util.GFileUtils;

public final class ConjurePlugin implements Plugin<Project> {

    // java project constants
    private static final String JAVA_GENERATED_SOURCE_DIRNAME = "src/generated/java";
    private static final String JAVA_GITIGNORE_DIRNAME = "src";
    private static final String JAVA_GITIGNORE_CONTENTS = "/generated/**/*.java\n";

    // gradle task constants
    private static final String TASK_COMPILE_CONJURE = "compileConjure";
    private static final String TASK_CLEAN = "clean";
    private static final String TASK_COPY_CONJURE_SOURCES = "copyConjureSourcesIntoBuild";

    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public ConjurePlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }

    @Override
    @SuppressWarnings("checkstyle:methodlength")
    public void apply(Project project) {
        project.getPlugins().apply(BasePlugin.class);
        ConjureExtension extension = project.getExtensions().create("conjure", ConjureExtension.class);

        // Conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        // Copy conjure imports into build directory
        File buildDir = new File(project.getBuildDir(), "conjure");

        // Copy conjure sources into build directory
        Copy copyConjureSourcesTask = project.getTasks().create(TASK_COPY_CONJURE_SOURCES, Copy.class);
        copyConjureSourcesTask.into(project.file(buildDir))
                .from(conjureSourceSet);

        copyConjureSourcesTask.doFirst(task -> {
            GFileUtils.deleteDirectory(buildDir);
        });

        final Task cleanTask = project.getTasks().findByName(TASK_CLEAN);
        cleanTask.dependsOn(project.getTasks().findByName("cleanCopyConjureSourcesIntoBuild"));

        // Set up conjure compile task
        Task conjureTask = project.getTasks().create(TASK_COMPILE_CONJURE, DefaultTask.class);
        applyDependencyForIdeTasks(project, conjureTask);

        final Supplier<Set<ExperimentalFeatures>> experimentalFeaturesSupplier = extension::getJavaExperimentalFeatures;
        setupConjureObjectsProject(project, experimentalFeaturesSupplier, conjureTask, copyConjureSourcesTask);
        setupConjureRetrofitProject(project, experimentalFeaturesSupplier, conjureTask, copyConjureSourcesTask);
        setupConjureJerseyProject(project, experimentalFeaturesSupplier, conjureTask, copyConjureSourcesTask);
        setupConjureTypescriptProject(project, extension, conjureTask, copyConjureSourcesTask);
        setupConjurePythonProject(project, extension, conjureTask, copyConjureSourcesTask);

        createCompileIrTask(project, conjureTask, copyConjureSourcesTask);
    }

    private static void setupConjureObjectsProject(
            Project project,
            Supplier<Set<ExperimentalFeatures>> experimentalFeaturesSupplier,
            Task conjureTask,
            Copy copyConjureSourcesTask) {

        String objectsProjectName = project.getName() + "-objects";
        if (project.findProject(objectsProjectName) != null) {
            project.project(objectsProjectName, (subproj) -> {
                subproj.getPluginManager().apply(JavaPlugin.class);
                addGeneratedToMainSourceSet(subproj);
                project.getTasks().create(
                        "compileConjureObjects",
                        CompileConjureJavaObjectsTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            File outputDir = subproj.file(JAVA_GENERATED_SOURCE_DIRNAME);
                            File gitignoreDir = subproj.file(JAVA_GITIGNORE_DIRNAME);
                            task.setOutputDirectory(outputDir);
                            task.setExperimentalFeatures(experimentalFeaturesSupplier);
                            conjureTask.dependsOn(task);
                            subproj.getTasks().getByName("compileJava").dependsOn(task);
                            applyDependencyForIdeTasks(subproj, task);
                            task.dependsOn(
                                    createWriteGitignoreTask(subproj, "gitignoreConjureObjects", gitignoreDir,
                                            JAVA_GITIGNORE_CONTENTS));
                        });

                Task cleanTask = project.getTasks().findByName(TASK_CLEAN);
                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureObjects"));
                subproj.getDependencies().add("compile", "com.palantir.conjure:conjure-java-lib");
            });
        }
    }

    private static void setupConjureRetrofitProject(
            Project project,
            Supplier<Set<ExperimentalFeatures>> experimentalFeaturesSupplier,
            Task conjureTask,
            Copy copyConjureSourcesTask) {

        String retrofitProjectName = project.getName() + "-retrofit";
        if (project.findProject(retrofitProjectName) != null) {
            String objectsProjectName = project.getName() + "-objects";
            if (project.findProject(objectsProjectName) == null) {
                throw new IllegalStateException(
                        String.format("Cannot enable '%s' without '%s'", retrofitProjectName, objectsProjectName));
            }

            project.project(retrofitProjectName, (subproj) -> {
                subproj.getPluginManager().apply(JavaPlugin.class);
                addGeneratedToMainSourceSet(subproj);
                project.getTasks().create(
                        "compileConjureRetrofit",
                        CompileConjureJavaServiceTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            File outputDir = subproj.file(JAVA_GENERATED_SOURCE_DIRNAME);
                            File gitignoreDir = subproj.file(JAVA_GITIGNORE_DIRNAME);
                            task.setOutputDirectory(outputDir);
                            task.setServiceGeneratorFactory(Retrofit2ServiceGenerator::new);
                            task.setExperimentalFeatures(experimentalFeaturesSupplier);
                            conjureTask.dependsOn(task);

                            subproj.getTasks().getByName("compileJava").dependsOn(task);
                            applyDependencyForIdeTasks(subproj, task);
                            task.dependsOn(
                                    createWriteGitignoreTask(subproj, "gitignoreConjureRetrofit", gitignoreDir,
                                            JAVA_GITIGNORE_CONTENTS));
                        });

                Task cleanTask = project.getTasks().findByName(TASK_CLEAN);
                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureRetrofit"));
                subproj.getDependencies().add("compile", project.findProject(objectsProjectName));
                subproj.getDependencies().add("compile", "com.squareup.retrofit2:retrofit");
            });
        }
    }

    private static void setupConjureJerseyProject(
            Project project,
            Supplier<Set<ExperimentalFeatures>> experimentalFeaturesSupplier,
            Task conjureTask,
            Copy copyConjureSourcesTask) {

        String jerseyProjectName = project.getName() + "-jersey";
        if (project.findProject(jerseyProjectName) != null) {
            String objectsProjectName = project.getName() + "-objects";
            if (project.findProject(objectsProjectName) == null) {
                throw new IllegalStateException(
                        String.format("Cannot enable '%s' without '%s'", jerseyProjectName, objectsProjectName));
            }

            project.project(project.getName() + "-jersey", (subproj) -> {
                subproj.getPluginManager().apply(JavaPlugin.class);
                addGeneratedToMainSourceSet(subproj);
                project.getTasks().create(
                        "compileConjureJersey",
                        CompileConjureJavaServiceTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            File outputDir = subproj.file(JAVA_GENERATED_SOURCE_DIRNAME);
                            File gitignoreDir = subproj.file(JAVA_GITIGNORE_DIRNAME);
                            task.setOutputDirectory(outputDir);
                            task.setServiceGeneratorFactory(JerseyServiceGenerator::new);
                            task.setExperimentalFeatures(experimentalFeaturesSupplier);
                            conjureTask.dependsOn(task);
                            subproj.getTasks().getByName("compileJava").dependsOn(task);
                            applyDependencyForIdeTasks(subproj, task);
                            task.dependsOn(
                                    createWriteGitignoreTask(subproj, "gitignoreConjureJersey", gitignoreDir,
                                            JAVA_GITIGNORE_CONTENTS));
                        });

                Task cleanTask = project.getTasks().findByName(TASK_CLEAN);
                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureJersey"));
                subproj.getDependencies().add("compile", project.findProject(objectsProjectName));
                subproj.getDependencies().add("compile", "javax.ws.rs:javax.ws.rs-api");
            });
        }
    }

    private static void setupConjureTypescriptProject(
            Project project,
            ConjureExtension extension,
            Task conjureTask,
            Copy copyConjureSourcesTask) {
        String typescriptProjectName = project.getName() + "-typescript";
        if (project.findProject(typescriptProjectName) != null) {
            project.project(typescriptProjectName, (subproj) -> {
                applyDependencyForIdeTasks(subproj, conjureTask);

                project.getTasks().create("compileConjureTypeScript",
                        CompileConjureTypeScriptTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            File outputDir = subproj.file("src");
                            task.setOutputDirectory(outputDir);
                            task.setNodeModulesOutputDirectory(new File(subproj.getBuildDir(), "node_modules"));
                            task.setServiceGenerator(new DefaultServiceGenerator());
                            task.setTypeGenerator(new DefaultTypeGenerator());
                            task.setErrorGenerator(new DefaultErrorGenerator());
                            task.setExperimentalFeatures(extension::getTypescriptExperimentalFeatures);
                            conjureTask.dependsOn(task);
                            task.dependsOn(
                                    createWriteGitignoreTask(
                                            subproj, "gitignoreConjureTypeScript", subproj.getProjectDir(),
                                            "*.ts\npackage.json\n"));
                        });

                Task cleanTask = project.getTasks().findByName(TASK_CLEAN);
                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureTypeScript"));
            });
        }
    }

    private static void setupConjurePythonProject(
            Project project,
            ConjureExtension extension,
            Task conjureTask,
            Copy copyConjureSourcesTask) {
        String pythonProjectName = project.getName() + "-python";
        if (project.findProject(pythonProjectName) != null) {
            project.project(pythonProjectName, (subproj) -> {
                applyDependencyForIdeTasks(subproj, conjureTask);

                project.getTasks().create("compileConjurePython",
                        CompileConjurePythonTask.class,
                        (task) -> {
                            task.setSource(copyConjureSourcesTask);
                            File outputDir = subproj.file("python");
                            task.setOutputDirectory(outputDir);
                            task.setClientGenerator(new ClientGenerator());
                            task.setExperimentalFeatures(extension::getPythonExperimentalFeatures);
                            conjureTask.dependsOn(task);
                            task.dependsOn(
                                    createWriteGitignoreTask(
                                            subproj, "gitignoreConjurePython", subproj.getProjectDir(),
                                            "*.py\n"));
                        });

                Task cleanTask = project.getTasks().findByName(TASK_CLEAN);
                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjurePython"));
            });
        }
    }

    private static void addGeneratedToMainSourceSet(Project subproj) {
        JavaPluginConvention javaPlugin = subproj.getConvention().findPlugin(JavaPluginConvention.class);
        javaPlugin.getSourceSets().getByName("main").getJava().srcDir(subproj.files(JAVA_GENERATED_SOURCE_DIRNAME));
    }

    private static void applyDependencyForIdeTasks(Project project, Task conjureTask) {
        project.getPlugins().withType(IdeaPlugin.class, plugin -> {
            Task task = project.getTasks().findByName("ideaModule");
            if (task != null) {
                task.dependsOn(conjureTask);
            }

            plugin.getModel().getModule().getSourceDirs().add(project.file(JAVA_GENERATED_SOURCE_DIRNAME));
            plugin.getModel().getModule().getGeneratedSourceDirs().add(project.file(JAVA_GENERATED_SOURCE_DIRNAME));
        });
        project.getPlugins().withType(EclipsePlugin.class, plugin -> {
            Task task = project.getTasks().findByName("eclipseClasspath");
            if (task != null) {
                task.dependsOn(conjureTask);
            }
        });
    }

    private static Task createWriteGitignoreTask(Project project, String taskName, File outputDir, String contents) {
        WriteGitignoreTask writeGitignoreTask = project.getTasks().create(taskName, WriteGitignoreTask.class);
        writeGitignoreTask.setOutputDirectory(outputDir);
        writeGitignoreTask.setContents(contents);
        return writeGitignoreTask;
    }

    private static Task createCompileIrTask(Project project, Task conjureTask, Copy copyConjureSourcesTask) {
        File irPath = Paths.get(project.getBuildDir().toString(), "conjure-ir", project.getName() + ".json").toFile();
        Task compileIr = project.getTasks().create("compileIr", CompileIrTask.class, (task) -> {
            task.setSource(copyConjureSourcesTask);
            task.setOutputFile(irPath);
        });

        conjureTask.dependsOn(compileIr);
        return compileIr;
    }
}

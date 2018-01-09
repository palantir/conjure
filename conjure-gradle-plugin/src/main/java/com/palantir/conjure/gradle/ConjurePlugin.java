/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import com.google.common.base.Preconditions;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.typescript.errors.DefaultErrorGenerator;
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
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.util.GFileUtils;

public class ConjurePlugin implements Plugin<Project> {

    private static final String EXTERNAL_IMPORTS_DIRNAME = "external-imports";
    private static final String JAVA_GENERATED_SOURCE_DIRNAME = "src/generated/java";
    private static final String JAVA_GITIGNORE_DIRNAME = "src";
    private static final String JAVA_GITIGNORE_CONTENTS = "/generated/**/*.java\n";

    private final SourceDirectorySetFactory sourceDirectorySetFactory;

    @Inject
    public ConjurePlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory;
    }

    @Override
    @SuppressWarnings("checkstyle:methodlength")
    public final void apply(Project project) {
        project.getPlugins().apply(BasePlugin.class);
        ConjureExtension extension = project.getExtensions().create("conjure", ConjureExtension.class, project);

        // Conjure code source set
        SourceDirectorySet conjureSourceSet = sourceDirectorySetFactory.create("conjure");
        conjureSourceSet.setSrcDirs(Collections.singleton("src/main/conjure"));
        conjureSourceSet.setIncludes(Collections.singleton("**/*.yml"));

        // Copy conjure imports into build directory
        File buildDir = new File(project.getBuildDir(), "conjure");

        // Copy conjure sources into build directory
        Copy copyConjureSourcesTask = project.getTasks().create("copyConjureSourcesIntoBuild", Copy.class);
        copyConjureSourcesTask.into(project.file(buildDir))
                .from(conjureSourceSet)
                .from(extension.getConjureImports(), action -> action.into(new File(EXTERNAL_IMPORTS_DIRNAME)));

        copyConjureSourcesTask.doFirst(task -> {
            if (!extension.getConjureImports().getFiles().isEmpty()) {
                Preconditions.checkState(
                        extension.getExperimentalFeatures().contains("GradleExternalImports"),
                        "Gradle-defined conjure imports are an experimental feature and must be enabled "
                                + "with the experimentalFeature 'GradleExternalImports' feature flag.");
            }
            GFileUtils.deleteDirectory(buildDir);
        });

        final Task cleanTask = project.getTasks().findByName("clean");
        cleanTask.dependsOn(project.getTasks().findByName("cleanCopyConjureSourcesIntoBuild"));

        // Set up conjure compile task
        Task conjureTask = project.getTasks().create("compileConjure", DefaultTask.class);
        applyDependencyForIdeTasks(project, conjureTask);

        final String objectsProjectName = project.getName() + "-objects";
        final String retrofitProjectName = project.getName() + "-retrofit";
        final String jerseyProjectName = project.getName() + "-jersey";
        final String typescriptProjectName = project.getName() + "-typescript";
        final String pythonProjectName = project.getName() + "-python";

        final Supplier<Set<ExperimentalFeatures>> experimentalFeaturesSupplier = extension::getJavaExperimentalFeatures;

        final Project objectsProject;
        if (project.findProject(objectsProjectName) != null) {
            objectsProject = project.project(objectsProjectName, (subproj) -> {
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

                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureObjects"));

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

                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureRetrofit"));

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

                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureJersey"));

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

                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjureTypeScript"));
            });
        }

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

                cleanTask.dependsOn(project.getTasks().findByName("cleanCompileConjurePython"));
            });
        }
    }

    static Set<File> excludeExternalImports(Set<File> files) {
        return files.stream()
                .filter(f -> !Objects.equals(f.getParentFile().getName(), EXTERNAL_IMPORTS_DIRNAME))
                .collect(Collectors.toSet());
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
}

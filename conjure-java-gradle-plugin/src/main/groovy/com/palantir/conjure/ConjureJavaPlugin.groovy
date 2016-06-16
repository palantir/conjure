/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure

import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention

class ConjureJavaPlugin implements Plugin<Project> {

    static def SOURCE_SET_NAME = 'conjure'
    static def SOURCE_DIR = 'src/main/conjure'
    static def JAVA_SOURCE_SET = 'generated'  // TODO(rfink) Make configurable
    static def OUTPUT_DIR = 'src/' + JAVA_SOURCE_SET + '/java'

    def SourceDirectorySetFactory sourceDirectorySetFactory
    def Project project

    @Inject
    public ConjureJavaPlugin(SourceDirectorySetFactory sourceDirectorySetFactory) {
        this.sourceDirectorySetFactory = sourceDirectorySetFactory
    }

    @Override
    void apply(Project project) {
        this.project = project
        project.pluginManager.apply(JavaPlugin)
        def conjureSourceSet = createConjureSourceSet()
        def compileJavaServerTask = createCompileJavaServerTask(conjureSourceSet)
        // TODO(rfink) Add client compilation task
        def generatedJavaSourceSet = createJavaSourceSet()

        project.tasks.compileJava.dependsOn compileJavaServerTask

        project.dependencies {
            compile generatedJavaSourceSet.output
        }
    }

    def createCompileJavaServerTask(SourceDirectorySet sourceSet) {
        ConjureCompileJavaServerTask task = project.tasks.create(
                "compile" + sourceSet.name.capitalize() + "JavaServer", ConjureCompileJavaServerTask)
        task.setSource(sourceSet)
        task.setOutputDirectory(project.projectDir.toPath().resolve(OUTPUT_DIR))
        task
    }

    def SourceDirectorySet createConjureSourceSet() {
        SourceDirectorySet sourceSet = sourceDirectorySetFactory.create(SOURCE_SET_NAME)
        sourceSet.srcDirs(SOURCE_DIR)
        sourceSet.include("**/*.yml")
        sourceSet
    }

    def createJavaSourceSet() {
        project.getConvention().getPlugin(JavaPluginConvention).sourceSets.create(JAVA_SOURCE_SET)
    }
}

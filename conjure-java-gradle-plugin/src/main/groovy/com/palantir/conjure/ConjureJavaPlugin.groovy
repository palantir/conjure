/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        def compileConjure = createCompileConjureTask(conjureSourceSet)
        def generatedJavaSourceSet = createJavaSourceSet()

        project.tasks.compileJava.dependsOn compileConjure

        project.dependencies {
            compile generatedJavaSourceSet.output
        }
    }

    def createCompileConjureTask(SourceDirectorySet sourceSet) {
        ConjureJavaCompileTask task =
                project.tasks.create("compile" + sourceSet.name.capitalize(), ConjureJavaCompileTask)
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

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

import com.energizedwork.spock.extensions.TempDirectory
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

public class GradleTestSpec extends Specification {
    @TempDirectory
    File testDir
    File buildFile

    def setup() {
        buildFile = file("build.gradle")
        println("Build directory: \n" + testDir.absolutePath)
    }

    protected def run(String... tasks) {
        return GradleRunner.create()
                .withProjectDir(testDir)
                .withArguments(tasks)
                .withPluginClasspath()
                .withDebug(true)
                .build()
    }

    @CompileStatic(TypeCheckingMode.SKIP)
    protected File createFile(String path, File baseDir = testDir) {
        File file = file(path, baseDir)
        if (!file.exists()) {
            assert file.parentFile.mkdirs() || file.parentFile.exists()
            file.createNewFile()
        }
        file
    }

    protected File file(String path, File baseDir = testDir) {
        def splitted = path.split('/')
        def directory = splitted.size() > 1 ? directory(splitted[0..-2].join('/'), baseDir) : baseDir
        def file = new File(directory, splitted[-1])
        file.createNewFile()
        file
    }

    protected File directory(String path, File baseDir = testDir) {
        new File(baseDir, path).with {
            mkdirs()
            it
        }
    }
}

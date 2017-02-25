/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle

import com.energizedwork.spock.extensions.TempDirectory
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class GradleTestSpec extends Specification {
    @TempDirectory
    File testDir

    def setup() {
        println("Build directory: " + testDir.absolutePath)
    }

    protected run(String... tasks) {
        return build(tasks).build()
    }

    protected build(String... tasks) {
        return GradleRunner.create()
                .withProjectDir(testDir)
                .withArguments(tasks)
                .withPluginClasspath()
                .withDebug(true)
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

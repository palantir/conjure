/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish

import com.energizedwork.spock.extensions.TempDirectory
import com.google.common.collect.Lists;
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

class GradleTestSpec extends Specification {
    @TempDirectory
    protected File testDir

    def setup() {
        println("Build directory: " + testDir.absolutePath)
    }

    protected run(String... tasks) {
        List<String> taskList = Lists.newArrayList(tasks);
        taskList.add("--stacktrace");
        return GradleRunner.create()
                .withProjectDir(testDir)
                .withArguments(taskList)
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

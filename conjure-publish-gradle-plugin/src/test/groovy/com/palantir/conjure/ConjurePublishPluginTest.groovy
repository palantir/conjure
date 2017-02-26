/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure

import com.google.common.io.Resources
import java.nio.charset.Charset
import org.gradle.testkit.runner.TaskOutcome

public class ConjurePublishPluginTest extends GradleTestSpec {

    def setup() {
        buildFile << """
        plugins {
            id 'com.palantir.gradle-conjure-publish'
        }

        repositories { jcenter() }
        """
    }

    def 'compileConjureTypescriptClient is up to date when no source files exist'() {
        when:
        def result = run("compileConjureTypescriptClient")

        then:
        result.task(":compileConjureTypescriptClient").outcome == TaskOutcome.NO_SOURCE
    }

    def 'compileConjureTypescriptClient compiles all source files'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        createSourceFile("b.yml", readResource("test-service-b.yml"))
        def result = run("compileConjureTypescriptClient")

        then:
        result.task(":compileConjureTypescriptClient").outcome == TaskOutcome.SUCCESS
        compiledConjureOutputFile("api/testServiceAImpl.ts").text.contains("export class TestServiceA")
        compiledConjureOutputFile("api/testServiceBImpl.ts").text.contains("export class TestServiceB")
    }

    def 'compileTypescriptJavascript compiles all typescript files'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("compileTypescriptJavascript")

        then:
        result.task(":compileConjureTypescriptClient").outcome == TaskOutcome.SUCCESS
        result.task(":compileTypescriptJavascript").outcome == TaskOutcome.SUCCESS
        compiledTypescriptOutputFile("src/api/testServiceAImpl.js").text.contains("var TestServiceA = ")
    }

    def 'bundleJavascript bundles javascript files'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("bundleJavascript")

        then:
        result.task(":compileConjureTypescriptClient").outcome == TaskOutcome.SUCCESS
        result.task(":compileTypescriptJavascript").outcome == TaskOutcome.SUCCESS
        result.task(":bundleJavascript").outcome == TaskOutcome.SUCCESS
        bundledJavascriptOutputFile("api/testServiceAImpl.js").text.contains("var TestServiceA = ")
        bundledJavascriptOutputFile("package.json").text.contains(String.format("\"name\": \"@elements/%s-conjure\"",
                testDir.getName()));
    }

    def createSourceFile(String fileName, String text) {
        createFile("src/main/conjure/" + fileName).text = text
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

    def compiledConjureOutputFile(String fileName) {
        return file("build/conjurePublish/typescriptClient/compileConjureOutput/" + fileName)
    }

    def compiledTypescriptOutputFile(String fileName) {
        return file("build/conjurePublish/typescriptClient/compileTypescriptOutput/" + fileName);
    }

    def bundledJavascriptOutputFile(String fileName) {
        return file("build/conjurePublish/typescriptClient/bundleJavascriptOutput/" + fileName);
    }
}

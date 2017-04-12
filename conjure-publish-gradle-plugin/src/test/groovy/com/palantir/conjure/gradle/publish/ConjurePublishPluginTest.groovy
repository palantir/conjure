/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish

import com.google.common.io.Resources
import java.nio.charset.Charset
import org.gradle.testkit.runner.TaskOutcome

class ConjurePublishPluginTest extends GradleTestSpec {

    def setup() {
        file('build.gradle') << """
        plugins {
            id 'com.palantir.typescript-publish'
        }

        publishTypeScript.scopeName = 'my-scope'
        """

        file('src/index.ts').text = readResource('src/index.ts')
        file('src/api/stringExample.ts').text = readResource('src/api/stringExample.ts')
        file('src/api/testServiceA.ts').text = readResource('src/api/testServiceA.ts')
        file('src/api/testServiceAImpl.ts').text = readResource('src/api/testServiceAImpl.ts')
    }

    def 'compileTypeScriptJavaScript compiles all typescript files'() {
        when:
        def result = run('compileTypeScriptJavaScript')

        then:
        result.task(':compileTypeScriptJavaScript').outcome == TaskOutcome.SUCCESS
        compiledTypescriptOutputFile('src/api/testServiceAImpl.js').text.contains('var TestServiceA = ')
    }

    def 'bundleJavaScript bundles javascript files'() {
        when:
        def result = run('bundleJavaScript')

        then:
        result.task(':compileTypeScriptJavaScript').outcome == TaskOutcome.SUCCESS
        result.task(':bundleJavaScript').outcome == TaskOutcome.SUCCESS
        bundledJavascriptOutputFile('api/testServiceAImpl.js').text.contains('var TestServiceA = ')
        bundledJavascriptOutputFile('package.json').text.contains(String.format('"name": "@my-scope/%s-conjure"',
                testDir.getName()))
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

    def compiledTypescriptOutputFile(String fileName) {
        return file('build/conjurePublish/typescriptClient/compileTypeScriptOutput/' + fileName)
    }

    def bundledJavascriptOutputFile(String fileName) {
        return file('build/conjurePublish/typescriptClient/bundleJavaScriptOutput/' + fileName)
    }
}

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
        """

        // first package
        file('src/@palantir/api/index.ts').text = readResource('src/api/index.ts')
        file('src/@palantir/api/stringExample.ts').text = readResource('src/api/stringExample.ts')
        file('src/@palantir/api/testServiceA.ts').text = readResource('src/api/testServiceA.ts')
        file('src/@palantir/api/testServiceAImpl.ts').text = readResource('src/api/testServiceAImpl.ts')

        // second package
        file('src/@palantir/api-foo/index.ts').text = readResource('src/api-foo/index.ts')
        file('src/@palantir/api-foo/stringExample.ts').text = readResource('src/api-foo/stringExample.ts')
        file('src/@palantir/api-foo/testServiceFoo.ts').text = readResource('src/api-foo/testServiceFoo.ts')
        file('src/@palantir/api-foo/testServiceFooImpl.ts').text = readResource('src/api-foo/testServiceFooImpl.ts')
    }

    def 'compileTypeScriptJavaScript compiles all typescript files'() {
        when:
        def result = run('compileTypeScriptJavaScript')

        then:
        result.task(':compileTypeScriptJavaScript').outcome == TaskOutcome.SUCCESS
        file('build/compileTypeScriptOutput/src/@palantir/api/testServiceAImpl.js').text.contains('var TestServiceA = ')
        file('build/compileTypeScriptOutput/src/@palantir/api-foo/testServiceFooImpl.js').text.contains('var TestServiceFoo = ')
    }

    def 'generatePackageJson generates package.json'() {
        when:
        def result = run('generatePackageJson')

        then:
        result.task(':compileTypeScriptJavaScript').outcome == TaskOutcome.SUCCESS
        result.task(':generatePackageJson').outcome == TaskOutcome.SUCCESS

        // first package
        file('build/generatePackageJsonOutput/@palantir/api/testServiceAImpl.js').text.contains('var TestServiceA = ')
        file('build/generatePackageJsonOutput/@palantir/api/package.json').text.contains('"name": "@palantir/api"');

        // second package
        file('build/generatePackageJsonOutput/@palantir/api-foo/testServiceFooImpl.js').text.contains('var TestServiceFoo = ')
        file('build/generatePackageJsonOutput/@palantir/api-foo/package.json').text.contains('"name": "@palantir/api-foo"');
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

}

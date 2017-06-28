/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish

import com.google.common.io.Resources
import java.nio.charset.Charset
import javax.ws.rs.core.HttpHeaders
import okhttp3.mockwebserver.MockWebServer
import org.gradle.testkit.runner.TaskOutcome

class ConjurePublishPluginTest extends GradleTestSpec {

    private MockWebServer server = new MockWebServer();

    def setup() {
        server.start(8888);

        file('build.gradle') << """
        plugins {
            id 'com.palantir.typescript-publish'
        }
        """

        // @palantir/api package
        file('src/@palantir/api/package.json').text = readResource('src/api/package.json')
        file('src/@palantir/api/index.ts').text = readResource('src/api/index.ts')
        file('src/@palantir/api/stringExample.ts').text = readResource('src/api/stringExample.ts')
        file('src/@palantir/api/testServiceA.ts').text = readResource('src/api/testServiceA.ts')
        file('src/@palantir/api/testServiceAImpl.ts').text = readResource('src/api/testServiceAImpl.ts')


        // @palantir/api is used by @palantir/api-foo, so copy into build/node_modules
        file('build/node_modules/@palantir/api/package.json').text = readResource('src/api/package.json')
        file('build/node_modules/@palantir/api/index.ts').text = readResource('src/api/index.ts')
        file('build/node_modules/@palantir/api/stringExample.ts').text = readResource('src/api/stringExample.ts')
        file('build/node_modules/@palantir/api/testServiceA.ts').text = readResource('src/api/testServiceA.ts')
        file('build/node_modules/@palantir/api/testServiceAImpl.ts').text = readResource('src/api/testServiceAImpl.ts')

        // second package, with dependency on api
        file('src/@palantir/api-foo/package.json').text = readResource('src/api-foo/package.json')
        file('src/@palantir/api-foo/index.ts').text = readResource('src/api-foo/index.ts')
        file('src/@palantir/api-foo/testServiceFoo.ts').text = readResource('src/api-foo/testServiceFoo.ts')
        file('src/@palantir/api-foo/testServiceFooImpl.ts').text = readResource('src/api-foo/testServiceFooImpl.ts')
    }

    def cleanup() {
        server.shutdown();
    }

    def 'compileTypeScriptJavaScript compiles all typescript files'() {
        when:
        def result = run('compileTypeScriptJavaScript')

        then:
        result.task(':compileTypeScriptJavaScript').outcome == TaskOutcome.SUCCESS
        file('build/compileTypeScriptOutput/@palantir/api/package.json').exists()
        file('build/compileTypeScriptOutput/@palantir/api/testServiceAImpl.js').text.contains('var TestServiceA = ')
        file('build/compileTypeScriptOutput/@palantir/api-foo/testServiceFooImpl.js').text.contains('var TestServiceFoo = ')
    }

    def 'generateNpmrc creates .npmrc file'() {
        given:
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'));

        when:
        def result = run('generateNpmrc', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.task(':generateNpmrc').outcome == TaskOutcome.SUCCESS

        // verify that scope is trimmed and auth header is set
        def request = server.takeRequest();
        request.getPath() == '/auth/palantir'
        request.getHeader(HttpHeaders.AUTHORIZATION) != null
        request.getHeader(HttpHeaders.AUTHORIZATION).startsWith('Basic')

        // first package
        file('build/generateNpmrcOutput/@palantir/api/.npmrc').exists()
        file('build/generateNpmrcOutput/@palantir/api/.npmrc').text.contentEquals(readResource('.npmrc'))

        // second package
        file('build/generateNpmrcOutput/@palantir/api-foo/.npmrc').exists()
        file('build/generateNpmrcOutput/@palantir/api-foo/.npmrc').text.contentEquals(readResource('.npmrc'))
    }

    def 'publishTypeScript runs npm publish'() {
        given:
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'))
        MockArtifactory.enqueueNoopPublish(server)

        when:
        def result = run('publishTypeScript', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.task(':compileTypeScriptJavaScript').outcome == TaskOutcome.SUCCESS
        result.task(':generateNpmrc').outcome == TaskOutcome.SUCCESS
        result.task(':publishTypeScript').outcome == TaskOutcome.SUCCESS
    }

    def 'publish depends on publishTypeScript'() {
        given:
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'))
        MockArtifactory.enqueueNoopPublish(server)

        when:
        def result = run('publish', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.task(':publishTypeScript').outcome == TaskOutcome.SUCCESS
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish

import com.google.common.io.Resources
import java.nio.charset.Charset
import javax.ws.rs.core.HttpHeaders
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import okhttp3.mockwebserver.MockWebServer
import org.gradle.api.tasks.Exec
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

class ConjurePublishPluginTest extends IntegrationSpec {

    private MockWebServer server = new MockWebServer();

    def setup() {
        server.start(8888);

        buildFile << "apply plugin: 'com.palantir.typescript-publish'"

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
        server.shutdown()
    }

    def 'compileTypeScriptJavaScript compiles all typescript files'() {
        when:
        runTasksSuccessfully('compileTypeScriptJavaScript')

        then:
        fileExists('build/compileTypeScriptOutput/@palantir/api/package.json')
        file('build/compileTypeScriptOutput/@palantir/api/testServiceAImpl.js').text.contains('var TestServiceA = ')
        file('build/compileTypeScriptOutput/@palantir/api-foo/testServiceFooImpl.js').text.contains('var TestServiceFoo = ')
    }

    def 'generateNpmrc creates .npmrc file'() {
        given:
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'))

        when:
        runTasksSuccessfully('generateNpmrc', '-PnpmRegistryUri=http://localhost:8888')

        then:
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
        ExecutionResult result = runTasksSuccessfully('publishTypeScript', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.wasExecuted(':compileTypeScriptJavaScript')
        result.wasExecuted(':generateNpmrc')
        result.wasExecuted(':publishTypeScript')
    }

    def 'publish depends on publishTypeScript'() {
        given:
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'))
        MockArtifactory.enqueueNoopPublish(server)

        when:
        ExecutionResult result = runTasksSuccessfully('publish', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.wasExecuted(':publishTypeScript')
    }

    @Unroll
    def 'runs on version of gradle: #version'() {
        when:
        gradleVersion = version
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'))
        MockArtifactory.enqueueNoopPublish(server)
        ExecutionResult result = runTasksSuccessfully('publish', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.success

        where:
        version << ['4.1', '4.0', '3.5', '3.4']
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

}

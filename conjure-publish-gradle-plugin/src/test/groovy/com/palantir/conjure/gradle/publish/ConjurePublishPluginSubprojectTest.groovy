/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle.publish

import com.google.common.io.Resources
import java.nio.charset.Charset
import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import nebula.test.multiproject.MultiProjectIntegrationHelper
import okhttp3.mockwebserver.MockWebServer
import org.gradle.testkit.runner.TaskOutcome

class ConjurePublishPluginSubprojectTest extends IntegrationSpec {

    private MockWebServer server = new MockWebServer();

    def setup() {
        server.start(8888);

        addSubproject('subproject', "apply plugin: 'com.palantir.typescript-publish'")

        file('subproject/src/@palantir/api/package.json').text = readResource('src/api/package.json')
        file('subproject/src/@palantir/api/index.ts').text = readResource('src/api/index.ts')
        file('subproject/src/@palantir/api/stringExample.ts').text = readResource('src/api/stringExample.ts')
        file('subproject/src/@palantir/api/testServiceA.ts').text = readResource('src/api/testServiceA.ts')
        file('subproject/src/@palantir/api/testServiceAImpl.ts').text = readResource('src/api/testServiceAImpl.ts')
    }

    def cleanup() {
        server.shutdown()
    }

    def 'publish depends on publishTypeScript'() {
        given:
        MockArtifactory.enqueueNpmrcBody(server, readResource('.npmrc'))
        MockArtifactory.enqueueNoopPublish(server)

        when:
        ExecutionResult result = runTasksSuccessfully('publish', '-PnpmRegistryUri=http://localhost:8888')

        then:
        result.wasExecuted(':subproject:publishTypeScript')
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle

import com.google.common.io.Resources
import java.nio.charset.Charset
import org.gradle.testkit.runner.TaskOutcome

class ConjurePluginTest extends GradleTestSpec {

    def setup() {
        file('settings.gradle') << """
        include 'conjure-def'
        include 'jersey-server'
        include 'jersey-client'
        include 'retrofit-client'
        include 'typescript-client'
        """

        file('conjure-def/src/main/conjure/api.yml') << """
        types:
          definitions:
            default-package: test.test.api
            objects:
              StringExample:
                fields:
                  string: string
        services:
          TestServiceA:
            name: Test Service A
            package: test.test.api

            endpoints:
              get:
                http: GET /get
                args:
                  object: StringExample
                returns: StringExample
        """

        directory('jersey-server')
        directory('jersey-client')
        directory('retrofit-client')
        directory('typescript-client')
    }

    def 'compile just jersey server'() {
        given:
        file('conjure-def/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            jerseyServer {
                output project(':jersey-server').file('src/main/java')
            }
        }
        """

        when:
        def result = run(':conjure-def:compileConjure')

        then:
        result.task(':conjure-def:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyServer').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyClient').outcome == TaskOutcome.SKIPPED

        file('jersey-server/src/main/java/test/test/api/StringExample.java').exists()
        !file('jersey-server/src/main/java/test/test/api/StringExample.java').text.contains('ignoreUnknown')

        file('jersey-server/src/main/java/.gitignore').exists()
        file('jersey-server/src/main/java/.gitignore').text.contains('*.java')
    }

    def 'compile just jersey client'() {
        given:
        file('conjure-def/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            jerseyClient {
                output project(':jersey-client').file('src/main/java')
            }
        }
        """

        when:
        def result = run(':conjure-def:compileConjure')

        then:
        result.task(':conjure-def:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyServer').outcome == TaskOutcome.SKIPPED
        result.task(':conjure-def:compileConjureJerseyClient').outcome == TaskOutcome.SUCCESS

        file('jersey-client/src/main/java/test/test/api/StringExample.java').exists()
        file('jersey-client/src/main/java/test/test/api/StringExample.java').text.contains('ignoreUnknown = true')

        file('jersey-client/src/main/java/.gitignore').exists()
        file('jersey-client/src/main/java/.gitignore').text.contains('*.java')
    }

    def 'compile just retrofit client'() {
        given:
        file('conjure-def/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            retrofitClient {
                output project(':retrofit-client').file('src/main/java')
            }
        }
        """

        when:
        def result = run(':conjure-def:compileConjure')

        then:
        result.task(':conjure-def:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyServer').outcome == TaskOutcome.SKIPPED
        result.task(':conjure-def:compileConjureJerseyClient').outcome == TaskOutcome.SKIPPED
        result.task(':conjure-def:compileConjureRetrofitClient').outcome == TaskOutcome.SUCCESS

        file('retrofit-client/src/main/java/test/test/api/TestServiceA.java').exists()
        file('retrofit-client/src/main/java/test/test/api/TestServiceA.java').text.contains('import retrofit2.')

        file('retrofit-client/src/main/java/.gitignore').exists()
        file('retrofit-client/src/main/java/.gitignore').text.contains('*.java')
    }

    def 'compile just typescript client'() {
        given:
        file('conjure-def/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            typeScriptClient {
                output project(':typescript-client').file('src')
            }
        }
        """

        when:
        def result = run(':conjure-def:compileConjure')

        then:
        result.task(':conjure-def:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureTypeScriptClient').outcome == TaskOutcome.SUCCESS

        file('typescript-client/src/index.ts').exists()
        file('typescript-client/src/index.ts').text.contains('api/stringExample')

        file('typescript-client/src/.gitignore').exists()
        file('typescript-client/src/.gitignore').text.contains('*.ts')
    }


    def 'compile just jersey server and client'() {
        given:
        file('conjure-def/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            jerseyServer {
                output project(':jersey-server').file('src/main/java')
            }
            jerseyClient {
                output project(':jersey-client').file('src/main/java')
            }
        }
        """

        when:
        def result = run(':conjure-def:compileConjure')

        then:
        result.task(':conjure-def:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyServer').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyClient').outcome == TaskOutcome.SUCCESS

        file('jersey-server/src/main/java/test/api/StringExample.java').exists()
        file('jersey-client/src/main/java/test/api/StringExample.java').exists()
        file('jersey-server/src/main/java/.gitignore').exists()
        file('jersey-client/src/main/java/.gitignore').exists()
    }

    def 'copies conjure imports into build directory and provides imports to conjure compiler'() {
        given:
        file('conjure-def/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            conjureImports files('external-import.yml')

            jerseyServer {
                output project(':jersey-server').file('src/main/java')
            }
            jerseyClient {
                output project(':jersey-client').file('src/main/java')
            }
        }
        """
        file('conjure-def/src/main/conjure/conjure.yml') << """
        types:
          conjure-imports:
            externalImport: external-imports/external-import.yml
            internalImport: internal-import.yml
          definitions:
            default-package: test.a.api
            objects:

        services:
          TestServiceA:
            name: Test Service A
            package: test.api

            endpoints:
              get:
                http: GET /get
                args:
                  object: externalImport.ExternalImport
                returns: internalImport.InternalImport
        """
        file('conjure-def/src/main/conjure/internal-import.yml') << """
        types:
          definitions:
            default-package: test.a.api
            objects:
              InternalImport:
                fields:
                  stringField: string
        """
        file('conjure-def/external-import.yml') << """
        types:
          definitions:
            default-package: test.b.api
            objects:
              ExternalImport:
                fields:
                  stringField: string
        """

        when:
        def result = run(':conjure-def:compileConjure')

        then:
        result.task(':conjure-def:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyServer').outcome == TaskOutcome.SUCCESS
        result.task(':conjure-def:compileConjureJerseyClient').outcome == TaskOutcome.SUCCESS

        file('conjure-def/build/conjure/imports/external-import.yml').exists()
        file('conjure-def/build/conjure/internal-import.yml').exists()
        file('conjure-def/build/conjure/conjure.yml').exists()

        file('jersey-server/src/main/java/test/api/TestServiceA.java').text.contains("import test.a.api.InternalImport;")
        file('jersey-server/src/main/java/test/api/TestServiceA.java').text.contains("import test.b.api.ExternalImport;")
    }

    def readResource(String name) {
        Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }
}

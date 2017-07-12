/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle

import com.google.common.io.Resources
import java.nio.charset.Charset
import org.gradle.testkit.runner.TaskOutcome

class ConjurePluginTest extends GradleTestSpec {

    def setup() {
        createFile('settings.gradle') << """
        include 'api'
        include 'api:api-objects'
        include 'api:api-jersey'
        include 'api:api-retrofit'
        include 'api:api-typescript'
        include 'api:api-python'
        include 'server'
        """

        createFile('build.gradle') << """
        buildscript {
            repositories {
                maven {
                    url 'https://artifactory.palantir.build/artifactory/all-jar/'
                }
            }

            dependencies {
                classpath 'com.netflix.nebula:nebula-dependency-recommender:3.1.0'
            }
        }
        allprojects {
            version '0.1.0'
            group 'com.palantir.conjure.test'

            repositories {
                maven {
                    url 'https://artifactory.palantir.build/artifactory/all-jar/'
                }
            }
            apply plugin: 'nebula.dependency-recommender'

            dependencyRecommendations {
                propertiesFile file: project.rootProject.file('versions.props')
            }

            configurations.all {
                resolutionStrategy {
                    failOnVersionConflict()
                }
            }
        }
        """

        createFile('api/build.gradle') << """
        plugins {
            id 'com.palantir.conjure'
        }
        """

        createFile('versions.props') << """
        com.fasterxml.jackson.*:* = 2.6.7
        com.google.guava:guava = 18.0
        com.palantir.conjure:conjure-java-lib = 0.25.0
        com.squareup.retrofit2:retrofit = 2.1.0
        javax.ws.rs:javax.ws.rs-api = 2.0.1
        """

        createFile('api/src/main/conjure/api.yml') << """
        types:
          definitions:
            default-package: test.test.api
            objects:
              StringExample:
                fields:
                  string: string
        services:
          TestServiceFoo:
            name: Test Service Foo
            package: test.test.api

            endpoints:
              post:
                http: POST /post
                args:
                  object: StringExample
                returns: StringExample
        """
    }

    def 'compileConjure generates code in subprojects'() {
        when:
        def result = run(':api:compileConjure')

        then:
        result.task(':api:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':api:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureObjects').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureJersey').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureRetrofit').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureTypeScript').outcome == TaskOutcome.SUCCESS

        // java
        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').exists()
        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').text.contains('ignoreUnknown')
        file('api/api-objects/src/generated/java/.gitignore').exists()
        file('api/api-objects/src/generated/java/.gitignore').text.contains('*.java')

        // typescript
        file('api/api-typescript/src/@test/api/stringExample.ts').exists()
        file('api/api-typescript/src/@test/api/package.json').exists()
        file('api/api-typescript/src/@test/api/index.ts').exists()
        file('api/api-typescript/src/.gitignore').exists()
        file('api/api-typescript/src/.gitignore').text.contains('*.ts')
        file('api/api-typescript/src/.gitignore').text.contains('package.json')
    }

    def 'pythonTask generates code when enabled'() {
        given:
        file('api/build.gradle').text = """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            conjureImports files('external-import.yml')
        }

        tasks.getByName('compileConjurePython').enabled = true
        """

        when:
        def result = run(':api:compileConjure')

        then:
        result.task(':api:compileConjurePython').outcome == TaskOutcome.SUCCESS
        file('api/api-python/python/api/__init__.py').exists()
    }

    def 'check code compiles'() {
        when:
        def result = run('check')

        then:
        result.task(':api:api-jersey:compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':api:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureJersey').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureObjects').outcome == TaskOutcome.SUCCESS

        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').exists()
        file('api/api-objects/src/generated/java/.gitignore').exists()
    }


    def 'check code compiles when run in parallel with multiple build targets'() {
        when:
        def result = run('--parallel', 'check', 'tasks')

        then:
        result.task(':api:api-objects:compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':api:api-jersey:compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':api:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureJersey').outcome == TaskOutcome.SUCCESS

        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').exists()
        file('api/api-objects/src/generated/java/.gitignore').exists()
    }

    def 'check publication'() {
        given:
        file('api/build.gradle').text = """
        plugins {
            id 'com.palantir.conjure'
        }

        subprojects {
            apply plugin: 'nebula.maven-base-publish'
            apply plugin: 'nebula.maven-resolved-dependencies'
            apply plugin: 'nebula.javadoc-jar'
            apply plugin: 'nebula.source-jar'
        }
        """

        file('server/build.gradle') << """
        buildscript {
            repositories {
                maven {
                    url 'https://artifactory.palantir.build/artifactory/all-jar/'
                }
            }
            dependencies {
                classpath 'com.netflix.nebula:nebula-publishing-plugin:4.4.4'
            }
        }
        apply plugin: 'java'
        apply plugin: 'nebula.maven-base-publish'
        apply plugin: 'nebula.maven-resolved-dependencies'
        apply plugin: 'nebula.javadoc-jar'
        apply plugin: 'nebula.source-jar'

        dependencies {
            compile project(':api:api-jersey')
            compile project(':api:api-retrofit') // safe to include both this and jersey, if necessary
        }
        """

        when:
        def result = run('--parallel', 'publishToMavenLocal')

        then:
        result.task(':api:api-jersey:compileJava').outcome == TaskOutcome.SUCCESS
        result.task(':api:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureJersey').outcome == TaskOutcome.SUCCESS

        new File(System.getProperty('user.home') + '/.m2/repository/com/palantir/conjure/test/').exists()
        new File(System.getProperty('user.home') +
                '/.m2/repository/com/palantir/conjure/test/api-jersey/0.1.0/api-jersey-0.1.0.pom').exists()
        new File(System.getProperty('user.home') +
                '/.m2/repository/com/palantir/conjure/test/server/0.1.0/server-0.1.0.pom').exists()
        new File(System.getProperty('user.home') +
                '/.m2/repository/com/palantir/conjure/test/server/0.1.0/server-0.1.0.pom').text.contains('>api-jersey<')
    }

    def 'copies conjure imports into build directory and provides imports to conjure compiler'() {
        given:
        file('api/build.gradle').text = """
        plugins {
            id 'com.palantir.conjure'
        }

        conjure {
            conjureImports files('external-import.yml')
        }
        """
        createFile('api/src/main/conjure/conjure.yml') << """
        types:
          conjure-imports:
            externalImport: external-imports/external-import.yml
            internalImport: internal-import.yml
          definitions:
            default-package: test.api.default
            objects:

        services:
          TestServiceFoo:
            name: Test Service Foo
            package: test.api.service

            endpoints:
              post:
                http: POST /post
                args:
                  object: externalImport.ExternalImport
                returns: internalImport.InternalImport
        """
        createFile('api/src/main/conjure/internal-import.yml') << """
        types:
          definitions:
            default-package: test.api.internal
            objects:
              InternalImport:
                fields:
                  stringField: string
        """
        createFile('api/external-import.yml') << """
        types:
          definitions:
            default-package: test.api.external
            objects:
              ExternalImport:
                fields:
                  stringField: string
        services:
          ExternalService:
            name: External Service
            package: test.api.external
            endpoints:
              post:
                http: POST /post
                returns: ExternalImport
        """

        when:
        def result = run(':api:compileConjure')

        then:
        result.task(':api:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':api:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureJersey').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureObjects').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureRetrofit').outcome == TaskOutcome.SUCCESS

        file('api/build/conjure/external-imports/external-import.yml').exists()
        file('api/build/conjure/internal-import.yml').exists()
        file('api/build/conjure/conjure.yml').exists()

        // java
        file('api/api-jersey/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.internal.InternalImport;')
        file('api/api-jersey/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.external.ExternalImport;')
        file('api/api-retrofit/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.internal.InternalImport;')
        file('api/api-retrofit/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.external.ExternalImport;')
        file('api/api-objects/src/generated/java/test/api/external/ExternalImport.java').exists() == false
        file('api/api-jersey/src/generated/java/test/api/external/ExternalService.java').exists() == false
        file('api/api-retrofit/src/generated/java/test/api/external/ExternalService.java').exists() == false

        // python
        file('api/api-python/python/service/__init__.py').text.contains(
                'from internal import InternalImport')
        file('api/api-python/python/service/__init__.py').text.contains(
                'from external import ExternalImport')
        file('api/api-python/python/external').exists() == false

        // typescript
        file('api/api-typescript/src/@api/service/testServiceFoo.ts').text.contains(
                'import { IInternalImport } from "@api/internal"');
        file('api/api-typescript/src/@api/service/testServiceFoo.ts').text.contains(
                'import { IExternalImport } from "@api/external"');
        file('api/api-typescript/src/@api/external').exists() == false
        file('api/api-typescript/build/node_modules/@api/external').exists()
    }

    def 'omitting a project from settings is sufficient to disable'() {
        given:
        file('settings.gradle').text = """
        include 'api'
        include 'api:api-objects'
        """

        when:
        def result = run(':api:compileConjure')

        then:
        result.task(':api:compileConjure').outcome == TaskOutcome.SUCCESS
        result.task(':api:processConjureImports').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureObjects').outcome == TaskOutcome.SUCCESS
        result.task(':api:compileConjureJersey') == null

        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').exists()
        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').text.contains('ignoreUnknown')
    }

    def 'including only the jersey project throws because objects project is missing'() {
        given:
        file('settings.gradle').text = """
        include 'api'
        include 'api:api-jersey'
        """

        when:
        def result = fail(':api:compileConjure')

        then:
        result.task(':api:compileConjureJersey') == null
    }

    def 'experimental features are disabled by default'() {
        given:
        createFile('api/src/main/conjure/union.yml') << """
        types:
          definitions:
            default-package: test.a.api
            objects:
              UnionTypeExample:
                union:
                  number: integer
        """

        when:
        def result = fail(':api:compileConjureObjects')

        then:
        result.task(':api:compileConjureObjects').outcome == TaskOutcome.FAILED
        result.output.contains(
                "Error: UnionTypes is an experimental feature of conjure-java that has not been enabled.")
    }

    def 'can enable experimental features'() {
        given:
        createFile('api/src/main/conjure/union.yml') << """
        types:
          definitions:
            default-package: test.a.api
            objects:
              UnionTypeExample:
                union:
                  number: integer
        """
        file('api/build.gradle') << """
        conjure {
            experimentalFeature "UnionTypes"
        }
        """

        when:
        def result = run(':api:compileConjureObjects')

        then:
        result.task(':api:compileConjureObjects').outcome == TaskOutcome.SUCCESS
    }

    def readResource(String name) {
        Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }
}

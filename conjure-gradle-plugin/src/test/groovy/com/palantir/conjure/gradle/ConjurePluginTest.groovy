/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gradle

import com.google.common.io.Resources
import java.nio.charset.Charset
import nebula.test.functional.ExecutionResult
import nebula.test.IntegrationSpec
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Unroll

class ConjurePluginTest extends IntegrationSpec {

    def setup() {
        createFile('settings.gradle') << '''
        include 'api'
        include 'api:api-objects'
        include 'api:api-jersey'
        include 'api:api-retrofit'
        include 'api:api-typescript'
        include 'api:api-python'
        include 'server'
        '''.stripIndent()

        createFile('build.gradle') << '''
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
        '''.stripIndent()

        createFile('api/build.gradle') << "apply plugin: 'com.palantir.conjure'"

        createFile('versions.props') << '''
        com.fasterxml.jackson.*:* = 2.6.7
        com.google.guava:guava = 18.0
        com.palantir.conjure:conjure-java-lib = 0.25.0
        com.squareup.retrofit2:retrofit = 2.1.0
        javax.ws.rs:javax.ws.rs-api = 2.0.1
        '''.stripIndent()

        createFile('api/src/main/conjure/api.yml') << '''
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
        '''.stripIndent()
    }

    def 'compileConjure generates code in subprojects'() {
        when:
        ExecutionResult result = runTasksSuccessfully(':api:compileConjure')

        then:
        result.wasExecuted(':api:compileConjure')
        result.wasExecuted(':api:processConjureImports')
        result.wasExecuted(':api:compileConjureObjects')
        result.wasExecuted(':api:compileConjureJersey')
        result.wasExecuted(':api:compileConjureRetrofit')
        result.wasExecuted(':api:compileConjureTypeScript')

        // java
        fileExists('api/api-objects/src/generated/java/test/test/api/StringExample.java')
        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').text.contains('ignoreUnknown')
        fileExists('api/api-objects/src/generated/java/.gitignore')
        file('api/api-objects/src/generated/java/.gitignore').text.contains('*.java')

        // typescript
        fileExists('api/api-typescript/src/@test/api/stringExample.ts')
        fileExists('api/api-typescript/src/@test/api/package.json')
        fileExists('api/api-typescript/src/@test/api/index.ts')
        fileExists('api/api-typescript/src/.gitignore')
        file('api/api-typescript/src/.gitignore').text.contains('*.ts')
        file('api/api-typescript/src/.gitignore').text.contains('package.json')
    }

    def 'pythonTask generates code when enabled'() {
        file('api/build.gradle').text = '''
        apply plugin: 'com.palantir.conjure'

        conjure {
            conjureImports files('external-import.yml')
        }

        tasks.getByName('compileConjurePython').enabled = true
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully(':api:compileConjure')

        then:
        result.wasExecuted(':api:compileConjurePython')
        fileExists('api/api-python/python/api/__init__.py')
    }

    def 'check code compiles'() {
        when:
        ExecutionResult result = runTasksSuccessfully('check')

        then:
        result.wasExecuted(':api:api-jersey:compileJava')
        result.wasExecuted(':api:processConjureImports')
        result.wasExecuted(':api:compileConjureJersey')
        result.wasExecuted(':api:compileConjureObjects')

        fileExists('api/api-objects/src/generated/java/test/test/api/StringExample.java')
        fileExists('api/api-objects/src/generated/java/.gitignore')
    }

    def 'check code compiles when run in parallel with multiple build targets'() {
        when:
        ExecutionResult result = runTasksSuccessfully('--parallel', 'check', 'tasks')

        then:
        result.wasExecuted(':api:api-objects:compileJava')
        result.wasExecuted(':api:api-jersey:compileJava')
        result.wasExecuted(':api:processConjureImports')
        result.wasExecuted(':api:compileConjureJersey')

        fileExists('api/api-objects/src/generated/java/test/test/api/StringExample.java')
        fileExists('api/api-objects/src/generated/java/.gitignore')
    }

    def 'clean cleans up src/generated/java'() {
        when:
        runTasksSuccessfully('compileJava')
        ExecutionResult result = runTasksSuccessfully('clean')

        then:
        result.wasExecuted(':api:api-jersey:cleanConjureJersey')
        result.wasExecuted(':api:api-objects:cleanConjureObjects')
        result.wasExecuted(':api:api-retrofit:cleanConjureRetrofit')

        !fileExists('api/api-jersey/src/generated/java')
        !fileExists('api/api-objects/src/generated/java')
        !fileExists('api/api-retrofit/src/generated/java')
    }

    def 'compileConjure creates build/conjure for root project'() {
        when:
        runTasksSuccessfully('compileConjure')

        then:
        fileExists('api/build/conjure')
    }

    def 'clean cleans up build/conjure for root project'() {
        when:
        runTasksSuccessfully('compileConjure')
        ExecutionResult result = runTasksSuccessfully('clean')

        then:
        result.wasExecuted(':api:cleanCopiedConjureSources')

        !fileExists('api/build/conjure')
    }

    def 'copyConjureSources runs cleanCopiedConjureSources'() {
        when:
        ExecutionResult result = runTasksSuccessfully(':api:copyConjureSources')

        then:
        result.wasExecuted(':api:cleanCopiedConjureSources')
    }

    def 'cleanCopiedConjureSources task is registered correctly as a dependency if the clean task exists already'() {
        file('build.gradle') << '''
            task clean(type: Delete) {
                // do nothing
            }
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully('clean')

        then:
        result.wasExecuted(':api:cleanCopiedConjureSources')
    }

    def 'check publication'() {
        file('build.gradle') << '''
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
        '''.stripIndent()

        file('api/build.gradle') << '''
        subprojects {
            apply plugin: 'nebula.maven-base-publish'
            apply plugin: 'nebula.maven-resolved-dependencies'
            apply plugin: 'nebula.javadoc-jar'
            apply plugin: 'nebula.source-jar'
        }
        '''.stripIndent()

        file('server/build.gradle') << '''
        apply plugin: 'java'
        apply plugin: 'nebula.maven-base-publish'
        apply plugin: 'nebula.maven-resolved-dependencies'
        apply plugin: 'nebula.javadoc-jar'
        apply plugin: 'nebula.source-jar'

        dependencies {
            compile project(':api:api-jersey')
            compile project(':api:api-retrofit') // safe to include both this and jersey, if necessary
        }
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully('--parallel', 'publishToMavenLocal')

        then:
        result.wasExecuted(':api:api-jersey:compileJava')
        result.wasExecuted(':api:processConjureImports')
        result.wasExecuted(':api:compileConjureJersey')

        new File(System.getProperty('user.home') + '/.m2/repository/com/palantir/conjure/test/').exists()
        new File(System.getProperty('user.home') +
                '/.m2/repository/com/palantir/conjure/test/api-jersey/0.1.0/api-jersey-0.1.0.pom').exists()
        new File(System.getProperty('user.home') +
                '/.m2/repository/com/palantir/conjure/test/server/0.1.0/server-0.1.0.pom').exists()
        new File(System.getProperty('user.home') +
                '/.m2/repository/com/palantir/conjure/test/server/0.1.0/server-0.1.0.pom').text.contains('>api-jersey<')
    }

    def 'copies conjure imports into build directory and provides imports to conjure compiler'() {
        file('api/build.gradle').text = '''
        apply plugin: 'com.palantir.conjure'

        conjure {
            conjureImports files('external-import.yml')
        }
        '''.stripIndent()

        createFile('api/src/main/conjure/conjure.yml') << '''
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
        '''.stripIndent()

        createFile('api/src/main/conjure/internal-import.yml') << '''
        types:
          definitions:
            default-package: test.api.internal
            objects:
              InternalImport:
                fields:
                  stringField: string
        '''.stripIndent()

        createFile('api/external-import.yml') << '''
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
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully(':api:compileConjure')

        then:
        result.wasExecuted(':api:compileConjure')
        result.wasExecuted(':api:processConjureImports')
        result.wasExecuted(':api:compileConjureJersey')
        result.wasExecuted(':api:compileConjureObjects')
        result.wasExecuted(':api:compileConjureRetrofit')

        fileExists('api/build/conjure/external-imports/external-import.yml')
        fileExists('api/build/conjure/internal-import.yml')
        fileExists('api/build/conjure/conjure.yml')

        // java
        file('api/api-jersey/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.internal.InternalImport;')
        file('api/api-jersey/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.external.ExternalImport;')
        file('api/api-retrofit/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.internal.InternalImport;')
        file('api/api-retrofit/src/generated/java/test/api/service/TestServiceFoo.java').text.contains(
                'import test.api.external.ExternalImport;')
        !fileExists('api/api-objects/src/generated/java/test/api/external/ExternalImport.java')
        !fileExists('api/api-jersey/src/generated/java/test/api/external/ExternalService.java')
        !fileExists('api/api-retrofit/src/generated/java/test/api/external/ExternalService.java')

        // python
        file('api/api-python/python/service/__init__.py').text.contains(
                'from internal import InternalImport')
        file('api/api-python/python/service/__init__.py').text.contains(
                'from external import ExternalImport')
        !fileExists('api/api-python/python/external')

        // typescript
        file('api/api-typescript/src/@api/service/testServiceFoo.ts').text.contains(
                'import { IInternalImport } from "@api/internal"')
        file('api/api-typescript/src/@api/service/testServiceFoo.ts').text.contains(
                'import { IExternalImport } from "@api/external"')
        !fileExists('api/api-typescript/src/@api/external')
        fileExists('api/api-typescript/build/node_modules/@api/external')
    }

    def 'omitting a project from settings is sufficient to disable'() {
        given:
        file('settings.gradle').text = '''
        include 'api'
        include 'api:api-objects'
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully(':api:compileConjure')

        then:
        result.wasExecuted(':api:compileConjure')
        result.wasExecuted(':api:processConjureImports')
        result.wasExecuted(':api:compileConjureObjects')
        !result.wasExecuted(':api:compileConjureJersey')

        fileExists('api/api-objects/src/generated/java/test/test/api/StringExample.java')
        file('api/api-objects/src/generated/java/test/test/api/StringExample.java').text.contains('ignoreUnknown')
    }

    def 'including only the jersey project throws because objects project is missing'() {
        given:
        file('settings.gradle').text = '''
        include 'api'
        include 'api:api-jersey'
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksWithFailure(':api:compileConjure')

        then:
        !result.wasExecuted(':api:compileConjureJersey')
    }

    def 'experimental features are disabled by default'() {
        createFile('api/src/main/conjure/union.yml') << '''
        types:
          definitions:
            default-package: test.a.api
            objects:
              UnionTypeExample:
                union:
                  number: integer
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksWithFailure(':api:compileConjureObjects')

        then:
        result.standardError.contains(
                "Error: UnionTypes is an experimental feature of conjure-java that has not been enabled.")
    }

    def 'can enable experimental features'() {
        createFile('api/src/main/conjure/union.yml') << '''
        types:
          definitions:
            default-package: test.a.api
            objects:
              UnionTypeExample:
                union:
                  number: integer
        '''.stripIndent()

        file('api/build.gradle') << '''
        conjure {
            experimentalFeature "UnionTypes"
        }
        '''.stripIndent()

        when:
        ExecutionResult result = runTasksSuccessfully(':api:compileConjureObjects')

        then:
        result.success
    }

    def 'works with afterEvaluate'() {
        file('build.gradle') << '''
            allprojects {
                afterEvaluate { p ->
                    if (p.tasks.findByPath('check') == null) {
                        p.tasks.create('check')
                    }
                }
            }
        '''.stripIndent()

        when:
        // doesn't matter what task is run, just need to trigger project evaluation
        ExecutionResult result = runTasksSuccessfully(':tasks')

        then:
        result.success
    }

    @Unroll
    def 'runs on version of gradle: #version'() {
        when:
        gradleVersion = version
        ExecutionResult result = runTasksSuccessfully('compileConjure')

        then:
        result.success

        where:
        version << ['4.1', '4.0', '3.5', '3.4']
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure

import com.google.common.io.Resources
import java.nio.charset.Charset
import org.gradle.testkit.runner.TaskOutcome

class ConjureJavaPluginTest extends GradleTestSpec {

    private static final String FAIL_ECLIPSE_CONTAINS_RESOURCES_GENERATED_FOLDER =
            "FAIL: Eclipse classpath contains build/resources/generated folder";

    def setup() {
        buildFile << """
        plugins {
            id 'com.palantir.gradle-conjure-java'
            id 'findbugs'
            id 'checkstyle'
            id 'eclipse'
            id 'idea'

            id 'nebula.source-jar' version '4.9.1'
        }

        repositories { jcenter() }

        dependencies {
            generatedCompile "com.fasterxml.jackson.core:jackson-databind:2.7.4"
            generatedCompile "javax.ws.rs:javax.ws.rs-api:2.0.1"
        }

        // Work-around to trust all HTTPS hosts.
        import javax.net.ssl.HostnameVerifier
        import javax.net.ssl.HttpsURLConnection
        import javax.net.ssl.SSLContext
        import javax.net.ssl.TrustManager
        import javax.net.ssl.X509TrustManager
        def nullTrustManager = [
                checkClientTrusted: { chain, authType ->  },
                checkServerTrusted: { chain, authType ->  },
                getAcceptedIssuers: { null }
        ]

        def nullHostnameVerifier = [
                verify: { hostname, session -> true }
        ]

        SSLContext sc = SSLContext.getInstance("SSL")
        sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
        HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

        afterEvaluate {
            sourceSets.generated.runtimeClasspath.each {
                if (it.toString().contains("build/resources/generated")) {
                    println "${FAIL_ECLIPSE_CONTAINS_RESOURCES_GENERATED_FOLDER}"
                }
            }
        }
        """
    }

    def 'compileConjure is up to date when no source files exist'() {
        when:
        def result = run("compileConjureJavaServer")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.UP_TO_DATE
    }

    def 'compileJava causes full task hierarchy to execute'() {
        when:
        def result = run("compileJava")

        then:
        result.task(":cleanGeneratedCode").outcome == TaskOutcome.UP_TO_DATE
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.UP_TO_DATE
        result.task(":compileGeneratedJava").outcome == TaskOutcome.UP_TO_DATE
        result.task(":compileJava").outcome == TaskOutcome.UP_TO_DATE
    }

    def 'compiles all source files'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        createSourceFile("b.yml", readResource("test-service-b.yml"))
        def result = run("compileConjureJavaServer")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        compiledFile("test/a/api/TestServiceA.java").text.contains("public interface TestServiceA")
        compiledFile("test/b/api/TestServiceB.java").text.contains("public interface TestServiceB")
    }

    def 'includes generated sources in sourceJar'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        createSourceFile("b.yml", readResource("test-service-b.yml"))
        def result = run("sourceJar")

        then:
        result.task(":sourceJar").outcome == TaskOutcome.SUCCESS
    }

    def 'can compile generated Java sourceSet'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("compileConjureJavaServer", "compileGeneratedJava")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        result.task(":compileGeneratedJava").outcome == TaskOutcome.SUCCESS
    }

    def 'main sourceSet depends on generated source set'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        createFile("src/main/java/Foo.java").text = """
            import test.a.api.SimpleObject;
            public class Foo {
                SimpleObject var = SimpleObject.builder().stringField("foo").build();
            }
        """
        def result = run("compileJava")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        result.task(":compileGeneratedJava").outcome == TaskOutcome.SUCCESS
        result.task(":compileJava").outcome == TaskOutcome.SUCCESS
    }

    def 'uses correct AuthHeader package'() {
        when:
        buildFile << """
            dependencies {
                generatedCompile 'com.palantir.tokens:auth-tokens:0.3.1'
            }
        """.stripIndent()
        createSourceFile("a.yml", readResource("test-service-with-auth.yml"))
        def result = run("compileConjureJavaServer", "compileGeneratedJava")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        result.task(":compileGeneratedJava").outcome == TaskOutcome.SUCCESS
    }

    def 'findbugs and checkstyle skipped for generated source set'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("build", "--stacktrace")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        result.task(":checkstyleGenerated").outcome == TaskOutcome.SKIPPED
        result.task(":findbugsGenerated").outcome == TaskOutcome.SKIPPED
    }

    def 'eclipse does not include build/resources/generated as a classpath entry'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("eclipse", "--stacktrace")

        then:
        result.task(":eclipse").outcome == TaskOutcome.SUCCESS
        !result.output.contains(FAIL_ECLIPSE_CONTAINS_RESOURCES_GENERATED_FOLDER)
    }

    def 'eclipse task depends on compileConjureJavaServer task'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("eclipse")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
    }

    def 'idea task depends on compileConjureJavaServer task'() {
        when:
        createSourceFile("a.yml", readResource("test-service-a.yml"))
        def result = run("idea")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
    }

    def 'generates Guava optionals by default'() {
        when:
        createSourceFile("a.yml", readResource("test-service-optional.yml"))
        def result = run("compileConjureJavaServer")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        compiledFile("test/a/api/SimpleObject.java").text.contains("import com.google.common.base.Optional;")
        !compiledFile("test/a/api/SimpleObject.java").text.contains("import java.util.Optional;")
    }

    def 'can configure generation of Java8 optionals instead of Guava optionals'() {
        when:
        createSourceFile("a.yml", readResource("test-service-optional.yml"))
        buildFile << """
            conjureJavaServer {
                optionalType = 'JAVA8'
            }
        """.stripIndent()
        def result = run("compileConjureJavaServer")

        then:
        result.task(":compileConjureJavaServer").outcome == TaskOutcome.SUCCESS
        !compiledFile("test/a/api/SimpleObject.java").text.contains("import com.google.common.base.Optional;")
        compiledFile("test/a/api/SimpleObject.java").text.contains("import java.util.Optional;")
    }

    def createSourceFile(String fileName, String text) {
        createFile("src/main/conjure/" + fileName).text = text
    }

    def readResource(String name) {
        return Resources.asCharSource(Resources.getResource(name), Charset.defaultCharset()).read()
    }

    def compiledFile(String fileName) {
        return file("src/generated/java/" + fileName)
    }
}

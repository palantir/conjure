/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.jsonschemagenerator

import nebula.test.IntegrationSpec
import nebula.test.functional.ExecutionResult
import org.gradle.internal.impldep.com.google.common.base.Splitter

import java.nio.file.Path
import java.util.stream.Collectors

class JsonSchemaGeneratorIntegrationSpec extends IntegrationSpec {
    def works() {
        Path classes = Path.of("build/classes/java/main").toAbsolutePath()
        String runtimeJars = Splitter.on('\n')
                .splitToStream(new File("build/runtimeClasspath").text)
                .map(jarPath -> "annotationProcessor files('" + jarPath + "')")
                .collect(Collectors.joining("\n"));

        // language=gradle
        buildFile << """
            apply plugin: 'java'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                annotationProcessor 'org.immutables:value:2.8.8'
                compileOnly 'org.immutables:value:2.8.8:annotations'
                
                annotationProcessor files('${classes}')
                compileOnly files('${classes}')

                ${runtimeJars}
            }
        """.stripIndent(true)

        // language=java
        writeJavaSourceFile '''
            package foo;

            import org.immutables.value.Value;
            import com.palantir.jsonschemagenerator.JsonSchema;
            
            @Value.Immutable
            @JsonSchema(outputLocation = "schema.json")
            interface Pair {
                String first();
                
                int second();
            }
        '''.stripIndent(true)

        when:
        runTasksSuccessfully('compileJava')

        then:
        1 == 1
    }

    ExecutionResult runTasksSuccessfully(String... tasks) {
        def result = super.runTasks(tasks);
        if (result.failure) {
            println result.standardError
            result.rethrowFailure()
        }
        result
    }
}

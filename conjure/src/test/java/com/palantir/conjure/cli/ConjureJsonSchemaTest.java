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

package com.palantir.conjure.cli;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ConjureJsonSchemaTest {

    private static final ObjectMapper objectMapper =
            new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module());
    private static final JsonSchema schema;

    static {
        try {
            schema = JsonSchemaFactory.getInstance(VersionFlag.V7)
                    .getSchema(Files.newInputStream(Paths.get("../conjure.schema.json")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("passing")
    void testPassingFiles(Path definitionPath) throws IOException {
        assertThat(schema.validate(objectMapper.readTree(Files.newBufferedReader(definitionPath))))
                .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("failing")
    void testFailingFiles(Path definitionPath) throws IOException {
        assertThat(schema.validate(objectMapper.readTree(Files.newBufferedReader(definitionPath))))
                .isNotEmpty();
    }

    static Set<Path> passing() {
        return walk(Paths.get("src/test/resources/jsonschema/passing"));
    }

    static Set<Path> failing() {
        return walk(Paths.get("src/test/resources/jsonschema/failing"));
    }

    private static Set<Path> walk(Path rootDir) {
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths.filter(path -> path.toFile().isFile()).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

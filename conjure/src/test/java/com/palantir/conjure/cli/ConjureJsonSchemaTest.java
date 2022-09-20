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
import com.networknt.schema.ValidationMessage;
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
    @MethodSource("definitionFiles")
    void testJsonSchema(Path definitionPath) throws IOException {
        // If the definition file contains comments of the form "# FAIL(JSONSCHEMA): error message" then we assert
        // that JSON schema validation fails.
        Set<ValidationMessage> messages =
                schema.validate(objectMapper.readTree(Files.newBufferedReader(definitionPath)));
        assertThat(messages).isEmpty();
    }

    private static Set<Path> definitionFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get("src/test/resources"))) {
            return paths.filter(path -> path.toFile().isFile()).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

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

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidatorTypeCode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test each of the definition files against the JSON schema definition.
 *
 * For any definition files that are intended to fail, the failing line should be annotated with a comment of the form:
 *
 *   # SCHEMA(foo, bar): some message explaining why this is an intended failure
 *
 * Where 'foo' and 'bar' are error codes from {@link ValidatorTypeCode}.
 */
public class ConjureJsonSchemaTest {

    private static final Pattern failurePattern = Pattern.compile("\\s*# SCHEMA\\(([\\w\\s,]+)\\):.*");
    private static final JsonSchema schema;

    static {
        try {
            schema = JsonSchemaFactory.getInstance(VersionFlag.V7)
                    .getSchema(Files.newInputStream(Paths.get("../conjure.schema.json")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("definitionFiles")
    void testJsonSchema(Path definitionPath) throws IOException {
        List<String> definitionLines = Files.readAllLines(definitionPath);

        // Set up a YAML ObjectMapper that collects the location of JsonNode's as they're parsed
        ParserCapturingJsonFactory jsonFactory = new ParserCapturingJsonFactory(new YAMLFactory());
        LocationTrackingNodeFactory nodeFactory =
                new LocationTrackingNodeFactory(JsonNodeFactory.instance, jsonFactory::getParser);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory).setNodeFactory(nodeFactory);

        // Parse the file using JsonPath, allowing us to find a JsonNode by its XPath
        DocumentContext ctx = JsonPath.parse(
                definitionPath.toFile(),
                Configuration.builder()
                        .jsonProvider(new JacksonJsonNodeJsonProvider(objectMapper))
                        .build());

        Set<ValidationMessage> messages = schema.validate(ctx.json());

        if (messages.isEmpty()) {
            // If the JSON schema passes validation, then ensure there are no failure comments in the file
            assertThat(definitionLines).noneMatch(failurePattern.asMatchPredicate());
            return;
        }

        // Otherwise, for each validation message check the failure comment contains the correct error codes
        messages.forEach(message -> {
            ValidatorTypeCode code = lookupTypeCode(message.getCode());
            JsonLocation location = nodeFactory.getLocationForNode(ctx.read(message.getPath()));

            // We should find an error comment on the line preceding the failure
            String precedingLine = definitionLines.get(location.getLineNr() - 2).trim();
            Matcher matcher = failurePattern.matcher(precedingLine);
            if (!matcher.matches()) {
                throw new AssertionError(
                        String.format("Unexpected JSON schema error '%s' %s", code, message.getMessage()));
            }

            // Extract the error codes from the comment and check against the message's error code
            Set<String> errorCodes = Splitter.on(',').splitToList(matcher.group(1)).stream()
                    .map(String::trim)
                    .collect(Collectors.toSet());
            assertThat(errorCodes)
                    .as("Checking schema error ValidatorTypeCode '%s'", code)
                    .contains(code.getValue());
        });
    }

    private static ValidatorTypeCode lookupTypeCode(String messageCode) {
        return Arrays.stream(ValidatorTypeCode.values())
                .filter(validatorTypeCode -> validatorTypeCode.getErrorCode().equals(messageCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unrecognized error code: " + messageCode));
    }

    private static Set<Path> definitionFiles() {
        try (Stream<Path> paths = Files.walk(Paths.get("src/test/resources"))) {
            return paths.filter(path -> path.toFile().isFile()).collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

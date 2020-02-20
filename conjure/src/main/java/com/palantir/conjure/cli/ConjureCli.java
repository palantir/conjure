/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.VisibleForTesting;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import picocli.CommandLine;

@CommandLine.Command(
        name = "conjure",
        description = "CLI to generate Conjure IR from Conjure YML definitions.",
        mixinStandardHelpOptions = true,
        subcommands = { ConjureCli.CompileCommand.class })
public final class ConjureCli implements Runnable {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    public static void main(String[] args) {
        CommandLine.run(new ConjureCli(), args);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    @CommandLine.Command(name = "compile",
            description = "Generate Conjure IR from Conjure YML definitions.",
            mixinStandardHelpOptions = true,
            usageHelpWidth = 120)
    public static final class CompileCommand implements Runnable {
        @CommandLine.Parameters(paramLabel = "<input>",
                description = "Path to the input conjure YML definition file, or directory containing multiple such "
                        + "files.",
                index = "0")
        private String input;

        @CommandLine.Parameters(paramLabel = "<output>",
                description = "Path to the output IR file.",
                index = "1")
        private String output;

        @CommandLine.Option(
                names = "--extensions",
                description = "")
        @Nullable
        private String extensions;

        @CommandLine.Unmatched
        @Nullable
        private List<String> unmatchedOptions;

        @SuppressWarnings("BanSystemErr")
        @Override
        public void run() {
            if (unmatchedOptions != null && !unmatchedOptions.isEmpty()) {
                System.err.println("Ignoring unknown options: " + unmatchedOptions);
            }
            CliConfiguration config = getConfiguration();
            generate(config);
        }

        @VisibleForTesting
        static void generate(CliConfiguration config) {
            ConjureDefinition definition = ConjureDefinition.builder()
                    .from(Conjure.parse(config.inputFiles()))
                    .extensions(config.extensions())
                    .build();
            try {
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(config.outputIrFile(), definition);
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize IR file to " + config.outputIrFile(), e);
            }
        }

        @VisibleForTesting
        CliConfiguration getConfiguration() {
            return CliConfiguration.create(
                    input,
                    output,
                    Optional.ofNullable(extensions)
                            .map(ConjureCli::parseExtensions)
                            .orElseGet(Collections::emptyMap));
        }
    }

    static Map<String, Object> parseExtensions(String extensions) {
        try {
            return OBJECT_MAPPER.readValue(extensions, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new SafeIllegalArgumentException("Failed to parse extensions", e);
        }
    }
}

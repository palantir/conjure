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
import com.google.common.base.Throwables;
import com.google.common.base.VerifyException;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.exceptions.ConjureException;
import com.palantir.conjure.parser.ConjureParser.CyclicImportException;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

@CommandLine.Command(
        name = "conjure",
        description = "CLI to generate Conjure IR from Conjure YML definitions.",
        mixinStandardHelpOptions = true,
        subcommands = ConjureCli.CompileCommand.class)
public final class ConjureCli implements Runnable {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

    public static void main(String[] args) {
        System.exit(prepareCommand().execute(args));
    }

    @VisibleForTesting
    static CommandLine prepareCommand() {
        return new CommandLine(new ConjureCli()).setExecutionExceptionHandler(new ExceptionHandler());
    }

    public static final class ExceptionHandler implements IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult _parseResult)
                throws Exception {
            if (ex == null) {
                return 0;
            }
            // For known internal errors, continue, as they're signaling errors on the input, not in the code
            Throwable rootCause = Throwables.getRootCause(ex);
            if (!(rootCause instanceof ConjureException)
                    && !(rootCause instanceof ParseException)
                    && !(rootCause instanceof CyclicImportException)
                    && !(rootCause instanceof VerifyException)) {
                throw ex;
            }
            if (!(commandLine.getCommand() instanceof ConjureCliCommand)) {
                throw ex;
            }
            ConjureCliCommand cmd = commandLine.getCommand();
            if (cmd.isVerbose()) {
                // If the command is verbose, print the full stack trace
                throw ex;
            }

            // Unpack errors; we don't care about where in the code the error comes from: the issue is in the supplied
            // conjure code. The stack doesn't help.
            Throwables.getCausalChain(ex)
                    .forEach(exception -> commandLine.getErr().println(exception.getMessage()));

            return -1;
        }
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    private interface ConjureCliCommand {
        boolean isVerbose();
    }

    @CommandLine.Command(
            name = "compile",
            description = "Generate Conjure IR from Conjure YML definitions.",
            mixinStandardHelpOptions = true,
            usageHelpWidth = 120)
    public static final class CompileCommand implements Runnable, ConjureCliCommand {
        @CommandLine.Parameters(
                paramLabel = "<input>",
                description = "Path to the input conjure YML definition file, or directory containing multiple such "
                        + "files.",
                index = "0")
        private String input;

        @CommandLine.Parameters(paramLabel = "<output>", description = "Path to the output IR file.", index = "1")
        private String output;

        @CommandLine.Option(names = "--verbose", description = "")
        private boolean verbose;

        @CommandLine.Option(names = "--extensions", description = "")
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

        @Override
        public boolean isVerbose() {
            return verbose;
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

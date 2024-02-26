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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureArgs;
import com.palantir.conjure.defs.SafetyDeclarationRequirements;
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
import java.util.stream.Collectors;
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

    public static void inProcessExecution(String[] args) {
        ExceptionHandler exceptionHandler = new ExceptionHandler();
        new CommandLine(new ConjureCli())
                .setExecutionExceptionHandler(exceptionHandler)
                .execute(args);
        exceptionHandler.maybeRethrowException();
    }

    @VisibleForTesting
    static CommandLine prepareCommand() {
        return new CommandLine(new ConjureCli()).setExecutionExceptionHandler(new ExceptionHandler());
    }

    public static final class ExceptionHandler implements IExecutionExceptionHandler {

        private Optional<Exception> thrownException;

        @Override
        public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult _parseResult)
                throws Exception {

            if (ex == null) {
                thrownException = Optional.ofNullable(ex);
                return 0;
            }
            if (!(commandLine.getCommand() instanceof ConjureCliCommand)) {
                thrownException = Optional.ofNullable(ex);
                throw ex;
            }
            ConjureCliCommand cmd = commandLine.getCommand();
            if (cmd.isVerbose()) {
                // If the command is verbose, print the full stack trace
                thrownException = Optional.ofNullable(ex);
                throw ex;
            }
            List<Throwable> chain = Throwables.getCausalChain(ex);

            // For known internal errors, change logging, as they're signaling errors on the input, not in the code
            if (chain.stream().noneMatch(ExceptionHandler::isConjureThrowable)) {
                thrownException = Optional.ofNullable(ex);
                throw ex;
            }

            // Unpack errors; we don't care about where in the code the error comes from: the issue is in the supplied
            // conjure code. The stack doesn't help.
            String message = chain.stream()
                    .map(exception -> (exception instanceof JsonMappingException)
                            ? handleJsonMappingException((JsonMappingException) exception)
                            : exception.getMessage())
                    .collect(Collectors.joining("\n"));
            commandLine.getErr().println(message);
            thrownException = Optional.ofNullable(new RuntimeException(message, ex));

            return -1;
        }

        private static String handleJsonMappingException(JsonMappingException jsonException) {
            String message = jsonException.getOriginalMessage();

            String path = jsonException.getPath().stream()
                    .map(Reference::getFieldName)
                    .collect(Collectors.joining(" -> "));

            return String.format("%s\n  @ %s", message, path);
        }

        private static boolean isConjureThrowable(Throwable rootCause) {
            return (rootCause instanceof ConjureException)
                    || (rootCause instanceof ParseException)
                    || (rootCause instanceof CyclicImportException);
        }

        private void maybeRethrowException() {
            thrownException.ifPresent(Throwables::throwIfUnchecked);
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

        @CommandLine.Option(
                names = "--requireSafety",
                description = "All components which allow safety declarations must declare safety.")
        private boolean requireSafety;

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
                    .from(Conjure.parse(ConjureArgs.builder()
                            .definitions(config.inputFiles())
                            .safetyDeclarations(
                                    config.requireSafety()
                                            ? SafetyDeclarationRequirements.REQUIRED
                                            : SafetyDeclarationRequirements.ALLOWED)
                            .build()))
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
                            .orElseGet(Collections::emptyMap),
                    requireSafety);
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

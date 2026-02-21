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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.google.common.collect.ImmutableList;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import picocli.CommandLine.PicocliException;

public final class ConjureCliTest {

    @TempDir
    public File folder;

    private File inputFile;
    private File outputFile;

    @BeforeEach
    public void before() throws IOException {
        File inputs = new File(folder, "inputs");
        assertThat(inputs.mkdir()).isTrue();
        inputFile = File.createTempFile("junit", ".yml", inputs);
        outputFile = new File(folder, "conjureIr.json");
    }

    @Test
    public void correctlyParseArguments() {
        String[] args = {
            "compile", inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), "--extensions", "{\"foo\": \"bar\"}"
        };
        CliConfiguration expectedConfiguration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(outputFile)
                .putExtensions("foo", "bar")
                .requireSafety(false)
                .build();
        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli())
                .parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThat(cmd.getConfiguration()).isEqualTo(expectedConfiguration);
    }

    @Test
    public void discoversFilesInDirectory() {
        String[] args = {"compile", folder.getAbsolutePath(), outputFile.getAbsolutePath()};
        CliConfiguration expectedConfiguration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(outputFile)
                .requireSafety(false)
                .build();
        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli())
                .parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThat(cmd.getConfiguration()).isEqualTo(expectedConfiguration);
    }

    @Test
    public void throwsWhenOutputIsDirectory() {
        String[] args = {"compile", folder.getAbsolutePath(), folder.getAbsolutePath()};
        AtomicReference<Exception> executionException = new AtomicReference<>();
        new CommandLine(new ConjureCli())
                .setExecutionExceptionHandler((ex, _commandLine, _parseResult) -> {
                    executionException.set(ex);
                    throw ex;
                })
                .execute(args);
        assertThat(executionException.get())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Output IR file should not be a directory");
    }

    @Test
    public void throwsWhenSubCommandIsNotCompile() {
        String[] args = {
            "compiles", folder.getAbsolutePath(), folder.getAbsolutePath(),
        };
        assertThatThrownBy(() -> CommandLine.populateCommand(new ConjureCli(), args))
                .isInstanceOf(PicocliException.class)
                .hasMessageContaining("Unmatched arguments");
    }

    @Test
    public void doesNotThrowWhenUnexpectedFeature() {
        String[] args = {"compile", inputFile.getAbsolutePath(), folder.getAbsolutePath(), "--foo"};
        CommandLine.populateCommand(new ConjureCli(), args);
    }

    @Test
    public void throwsWhenInvalidExtensions() {
        String[] args = {"compile", inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), "--extensions", "foo"};
        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli())
                .parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThatThrownBy(cmd::getConfiguration)
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasMessage("Failed to parse extensions");

        assertThatThrownBy(() -> ConjureCli.inProcessExecution(args))
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasMessage("Failed to parse extensions");
    }

    @Test
    public void generatesCode() {
        CliConfiguration configuration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(
                        new File("src/test/resources/complex/api.yml"),
                        new File("src/test/resources/complex/api-2.yml")))
                .outputIrFile(outputFile)
                .requireSafety(false)
                .build();
        ConjureCli.CompileCommand.generate(configuration);
        assertThat(outputFile).exists();
    }

    @Test
    public void canRequireSafetyInfo() {
        CliConfiguration configuration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(new File("src/test/resources/complex/api.yml")))
                .outputIrFile(outputFile)
                .requireSafety(true)
                .build();
        assertThatThrownBy(() -> ConjureCli.CompileCommand.generate(configuration))
                .hasMessageContaining("must declare log safety");
    }

    @Test
    public void generatesCleanError_unknown() {
        String[] args = {"compile", "src/test/resources/simple-error.yml", outputFile.getAbsolutePath()};

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ConjureCli.prepareCommand().setErr(printWriter).execute(args);
        printWriter.flush();
        assertThat(stringWriter.toString())
                .contains("Encountered error trying to parse file")
                .contains("src/test/resources/simple-error.yml")
                .contains("Unknown LocalReferenceType: TypeName{name=UnknownType}");
        assertThat(outputFile).doesNotExist();

        assertThatThrownBy(() -> ConjureCli.inProcessExecution(args))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Encountered error trying to parse file")
                .hasMessageContaining("src/test/resources/simple-error.yml")
                .hasMessageContaining("Unknown LocalReferenceType: TypeName{name=UnknownType}");
        assertThat(outputFile).doesNotExist();
    }

    @Test
    public void generatesCleanError_map() {
        String[] args = {"compile", "src/test/resources/key-error.yml", outputFile.getAbsolutePath()};

        String expectedErrorMessage =
                "Illegal map key found in union SimpleUnion in member optionA. Map keys can only be primitive"
                        + " Conjure types.\n";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ConjureCli.prepareCommand().setErr(printWriter).execute(args);
        printWriter.flush();
        assertThat(stringWriter.toString()).isEqualTo(expectedErrorMessage);
        assertThat(outputFile).doesNotExist();

        assertThatThrownBy(() -> ConjureCli.inProcessExecution(args))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedErrorMessage);
        assertThat(outputFile).doesNotExist();
    }

    @Test
    public void generatesCleanError_unique_name() {
        String[] args = {"compile", "src/test/resources/unique-name-error", outputFile.getAbsolutePath()};

        String expectedErrorMessage = "Type, error, and service names must be unique across locally defined and "
                + "imported types/errors:\n"
                + "Found duplicate name: test.api.ConflictingName\n"
                + "Known names:\n"
                + " - test.api.UniqueName\n"
                + " - test.api.UniqueName2\n"
                + " - test.api.ConflictingName\n";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ConjureCli.prepareCommand().setErr(printWriter).execute(args);
        printWriter.flush();
        assertThat(stringWriter.toString()).isEqualTo(expectedErrorMessage);
        assertThat(outputFile).doesNotExist();

        assertThatThrownBy(() -> ConjureCli.inProcessExecution(args))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedErrorMessage);
        assertThat(outputFile).doesNotExist();
    }

    @Test
    public void generatesCleanError_invalid_json() {
        String[] args = {"compile", "src/test/resources/invalid-json.yml", outputFile.getAbsolutePath()};

        String expectedErrorMessage = "src/test/resources/invalid-json.yml:\n"
                + "Cannot build FieldDefinition, some of required attributes are not set [type]\n"
                + "  @ types -> definitions -> objects -> InvalidJson -> union -> optionA\n"
                + "Cannot build FieldDefinition, some of required attributes are not set [type]\n";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ConjureCli.prepareCommand().setErr(printWriter).execute(args);
        printWriter.flush();
        assertThat(stringWriter.toString()).startsWith("Error while parsing").endsWith(expectedErrorMessage);
        assertThat(outputFile).doesNotExist();

        assertThatThrownBy(() -> ConjureCli.inProcessExecution(args))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(expectedErrorMessage);
        assertThat(outputFile).doesNotExist();
    }

    @Test
    public void throwsWhenInvalidDefinition() throws Exception {
        CliConfiguration configuration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(folder)
                .requireSafety(false)
                .build();
        assertThatThrownBy(() -> ConjureCli.CompileCommand.generate(configuration))
                .rootCause()
                .isInstanceOf(MismatchedInputException.class);
    }

    @Test
    public void doesNotThrowWhenCommandIsSuccessful() {
        String[] args = {"compile", "src/test/resources/test-service.yml", outputFile.getAbsolutePath()};
        ConjureCli.inProcessExecution(args);
    }

    @Test
    public void ignoresNonYamlFilesInDirectory() throws IOException {
        File.createTempFile("ignore", ".test", folder);
        String[] args = {"compile", folder.getAbsolutePath(), outputFile.getAbsolutePath()};
        CliConfiguration expectedConfiguration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(outputFile)
                .requireSafety(false)
                .build();
        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli())
                .parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThat(cmd.getConfiguration()).isEqualTo(expectedConfiguration);
    }

    @Test
    public void throwsIfSingleInputFileIsNotYaml() throws IOException {
        File nonYamlInputFile = File.createTempFile("ignore", ".test", folder);
        String[] args = {
            "compile",
            nonYamlInputFile.getAbsolutePath(),
            outputFile.getAbsolutePath(),
            "--extensions",
            "{\"foo\": \"bar\"}"
        };

        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli())
                .parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThatThrownBy(cmd::getConfiguration)
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("Failed to resolve input files")
                .cause()
                .hasMessageContaining("Input is not an existing YAML file or directory");
    }
}

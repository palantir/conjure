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

import com.google.common.collect.ImmutableList;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;
import picocli.CommandLine.PicocliException;
import picocli.CommandLine;

public final class ConjureCliTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File inputFile;
    private File outputFile;

    @Before
    public void before() throws IOException {
        File inputs = folder.newFolder("inputs");
        inputFile = File.createTempFile("junit", ".yml", inputs);
        outputFile = new File(folder.getRoot(), "conjureIr.json");
    }

    @Test
    public void correctlyParseArguments() {
        String[] args = {
                "compile",
                inputFile.getAbsolutePath(),
                outputFile.getAbsolutePath(),
                "--extensions", "{\"foo\": \"bar\"}"};
        CliConfiguration expectedConfiguration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(outputFile)
                .putExtensions("foo", "bar")
                .build();
        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli()).parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThat(cmd.getConfiguration()).isEqualTo(expectedConfiguration);
    }

    @Test
    public void discoversFilesInDirectory() {
        String[] args = {"compile", folder.getRoot().getAbsolutePath(), outputFile.getAbsolutePath()};
        CliConfiguration expectedConfiguration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(outputFile)
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
        String[] args = {"compile", folder.getRoot().getAbsolutePath(), folder.getRoot().getAbsolutePath()};
        AtomicReference<Exception> executionException = new AtomicReference<>();
        new CommandLine(new ConjureCli())
                .setExecutionExceptionHandler((ex, _commandLine, _parseResult) -> {
                    executionException.set(ex);
                    throw ex;
                }).execute(args);
        assertThat(executionException.get())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Output IR file should not be a directory");
    }

    @Test
    public void throwsWhenSubCommandIsNotCompile() {
        String[] args = {
                "compiles",
                folder.getRoot().getAbsolutePath(),
                folder.getRoot().getAbsolutePath(),
        };
        assertThatThrownBy(() -> CommandLine.populateCommand(new ConjureCli(), args))
                .isInstanceOf(PicocliException.class)
                .hasMessageContaining("Unmatched arguments");
    }

    @Test
    public void doesNotThrowWhenUnexpectedFeature() {
        String[] args = {
                "compile",
                inputFile.getAbsolutePath(),
                folder.getRoot().getAbsolutePath(),
                "--foo"
        };
        CommandLine.populateCommand(new ConjureCli(), args);
    }

    @Test
    public void throwsWhenInvalidExtensions() {
        String[] args = {"compile", inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), "--extensions", "foo"};
        ConjureCli.CompileCommand cmd = new CommandLine(new ConjureCli()).parseArgs(args)
                .asCommandLineList()
                .get(1)
                .getCommand();
        assertThatThrownBy(cmd::getConfiguration)
                .isInstanceOf(SafeIllegalArgumentException.class)
                .hasMessage("Failed to parse extensions");
    }

    @Test
    public void generatesCode() {
        CliConfiguration configuration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(new File("src/test/resources/test-service.yml")))
                .outputIrFile(outputFile)
                .build();
        ConjureCli.CompileCommand.generate(configuration);
        assertThat(outputFile.isFile()).isTrue();
    }

    @Test
    public void throwsWhenInvalidDefinition() throws Exception {
        CliConfiguration configuration = CliConfiguration.builder()
                .inputFiles(ImmutableList.of(inputFile))
                .outputIrFile(folder.newFolder())
                .build();
        assertThatThrownBy(() -> ConjureCli.CompileCommand.generate(configuration))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MismatchedInputException");
    }
}

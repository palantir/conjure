/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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

import com.google.common.base.Throwables;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.commons.io.output.TeeOutputStream;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * An {@link IExecutionExceptionHandler} that captures the exception thrown by the delegate and rethrows it later.
 * It is used by {@code inProcessExecution}s to re-throw the exceptions thrown by the command line execution.
 */
public final class ThrowableExceptionHandler implements IExecutionExceptionHandler {

    private Optional<Exception> thrownException = Optional.empty();
    private IExecutionExceptionHandler delegate;

    public ThrowableExceptionHandler(IExecutionExceptionHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, ParseResult parseResult)
            throws Exception {
        ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
        commandLine.setErr(new PrintWriter(new TeeOutputStream(System.err, bytesStream), true, StandardCharsets.UTF_8));
        try {
            delegate.handleExecutionException(ex, commandLine, parseResult);
        } catch (Exception e) {
            thrownException = Optional.of(e);
            throw e;
        }
        String errorOutput = bytesStream.toString(StandardCharsets.UTF_8);
        if (!errorOutput.isEmpty()) {
            thrownException = Optional.ofNullable(new RuntimeException(errorOutput));
        }
        return -1;
    }

    public void maybeRethrowException() {
        thrownException.ifPresent(Throwables::throwIfUnchecked);
    }
}

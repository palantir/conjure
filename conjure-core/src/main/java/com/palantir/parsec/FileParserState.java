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

package com.palantir.parsec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class FileParserState implements ParserState {

    private final StringParserState delegate;

    public FileParserState(File in) {
        try {
            StringBuilder sb = new StringBuilder(new String(Files.readAllBytes(in.toPath()), StandardCharsets.UTF_8));

            delegate = new StringParserState(sb);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Cannot find file '" + in.getAbsolutePath() + "'", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file '" + in.getAbsolutePath() + "'", e);
        }
    }

    @Override
    public int curr() {
        return delegate.curr();
    }

    @Override
    public int next() {
        return delegate.next();
    }

    @Override
    public void mark() {
        delegate.mark();
    }

    @Override
    public void release() {
        delegate.release();
    }

    @Override
    public void rewind() {
        delegate.rewind();
    }

    @Override
    public int getLine() {
        return delegate.getLine();
    }

    @Override
    public int getCharPosition() {
        return delegate.getLine();
    }
}

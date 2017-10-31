/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
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

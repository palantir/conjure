/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.palantir.conjure.gen.python.poet.PythonFile;
import com.palantir.conjure.gen.python.poet.PythonPoetWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DefaultPythonFileWriter implements PythonFileWriter {

    private final Path basePath;

    public DefaultPythonFileWriter(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public void writePythonFile(PythonFile file) {
        Path filePath = basePath.resolve(PythonFileWriter.getPath(file));
        try {
            Files.createDirectories(filePath.getParent());
            try (OutputStream os = Files.newOutputStream(filePath);
                    PrintStream ps = new PrintStream(os)) {
                PythonPoetWriter writer = new PythonPoetWriter(ps);
                writer.emit(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

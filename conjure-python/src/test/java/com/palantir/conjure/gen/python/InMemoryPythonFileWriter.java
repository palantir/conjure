/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import com.google.common.collect.Maps;
import com.palantir.conjure.gen.python.poet.PythonFile;
import com.palantir.conjure.gen.python.poet.PythonPoetWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public final class InMemoryPythonFileWriter implements PythonFileWriter {

    private final Map<Path, String> pythonFiles = Maps.newHashMap();

    public Map<Path, String> getPythonFiles() {
        return pythonFiles;
    }

    @Override
    public void writePythonFile(PythonFile pythonFile) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream(baos)) {
            PythonPoetWriter poetWriter = new PythonPoetWriter(printStream);

            poetWriter.emit(pythonFile);
            byte[] bytes = baos.toByteArray();

            pythonFiles.put(PythonFileWriter.getPath(pythonFile), new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

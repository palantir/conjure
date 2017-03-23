/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Splitter;
import com.palantir.conjure.gen.python.poet.PythonFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public interface PythonFileWriter {

    void writePythonFile(PythonFile file);

    static Path getPath(PythonFile file) {
        List<String> components = Splitter.on(".").splitToList(file.packageName());
        checkState(!components.isEmpty());
        Path packageDir = Paths.get(components.get(0), components.subList(1, components.size()).toArray(new String[0]));
        return packageDir.resolve("__init__.py");
    }

}

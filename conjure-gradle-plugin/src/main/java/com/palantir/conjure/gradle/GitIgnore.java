/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public final class GitIgnore {
    private GitIgnore() {}

    public static void writeGitIgnore(File directory, String contents) throws IOException {
        Files.write(directory.toPath().resolve(".gitignore"), contents.getBytes(StandardCharsets.UTF_8));
    }
}

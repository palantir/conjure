/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.util;

import com.google.common.base.Preconditions;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Tools for a better JavaPoet. */
public final class Goethe {

    private static final Formatter JAVA_FORMATTER =
            new Formatter(JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build());

    private Goethe() {}

    /** Formats the given Java file and emits it to the appropriate directory under {@code baseDir}. */
    public static Path formatAndEmit(JavaFile file, Path baseDir) {
        try {
            Path outputFile = getFilePath(file, baseDir);
            StringBuilder code = new StringBuilder();
            file.writeTo(code);

            CharSink sink = com.google.common.io.Files.asCharSink(outputFile.toFile(), StandardCharsets.UTF_8);
            JAVA_FORMATTER.formatSource(CharSource.wrap(code), sink);
            return outputFile;
        } catch (IOException | FormatterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the full path for the given Java file and Java base dir. In a nutshell, turns packages into directories,
     * e.g., {@code com.foo.bar.MyClass -> /<baseDir>/com/foo/bar/MyClass.java} and creates all directories.
     * Implementation taken from JavaPoet's {@link JavaFile#writeTo(File)}.
     */
    private static Path getFilePath(JavaFile file, Path baseDir) throws IOException {
        Preconditions.checkArgument(Files.notExists(baseDir) || Files.isDirectory(baseDir),
                "path %s exists but is not a directory.", baseDir);
        Path outputDirectory = baseDir;
        if (!file.packageName.isEmpty()) {
            for (String packageComponent : file.packageName.split("\\.")) {
                outputDirectory = outputDirectory.resolve(packageComponent);
            }
            Files.createDirectories(outputDirectory);
        }

        return outputDirectory.resolve(file.typeSpec.name + ".java");
    }
}

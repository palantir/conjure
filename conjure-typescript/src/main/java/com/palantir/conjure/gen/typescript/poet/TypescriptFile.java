/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptFile {

    String TS_EXTENSION = ".ts";

    List<Emittable> emittables();
    Set<ImportStatement> imports();
    String name();
    String parentFolderPath();

    default void writeTo(File file) throws IOException {
        String name = name();
        name = StringUtils.uncapitalize(name);
        File thisFile = new File(new File(file, parentFolderPath()), name + TS_EXTENSION);
        Files.createDirectories(thisFile.getParentFile().toPath());
        if (!thisFile.exists()) {
            Files.createFile(thisFile.toPath());
        }
        try (PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(thisFile)),
                false, StandardCharsets.UTF_8.name())) {
            TypescriptPoetWriter writer = new TypescriptPoetWriter(output);
            imports().stream().sorted().forEach(emittable -> emittable.emit(writer));
            if (!imports().isEmpty()) {
                writer.writeLine();
            }
            emittables().forEach(emittable -> emittable.emit(writer));
        }
    }

    static ImmutableTypescriptFile.Builder builder() {
        return ImmutableTypescriptFile.builder();
    }
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        File thisFile = file.toPath().resolve(path() + TS_EXTENSION).toFile();
        Files.createDirectories(thisFile.getParentFile().toPath());
        if (!thisFile.exists()) {
            Files.createFile(thisFile.toPath());
        }
        try (PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(thisFile)),
                false, StandardCharsets.UTF_8.name())) {
            writeTo(output);
        }
    }

    default void writeTo(PrintStream output) {
        TypescriptPoetWriter writer = new TypescriptPoetWriter(output);
        imports().stream().sorted().forEach(emittable -> emittable.emit(writer));
        if (!imports().isEmpty()) {
            writer.writeLine();
        }
        emittables().forEach(emittable -> emittable.emit(writer));
    }

    default String writeToString() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream output = new PrintStream(baos, false, StandardCharsets.UTF_8.name())) {
            writeTo(output);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    default Path path() {
        String name = StringUtils.uncapitalize(name());
        return Paths.get(parentFolderPath()).resolve(name);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptFile.Builder {}
}

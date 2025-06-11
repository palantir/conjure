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

package com.palantir.conjure.cli;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CliConfiguration {
    abstract Collection<File> inputFiles();

    abstract File outputIrFile();

    abstract Map<String, Object> extensions();

    abstract boolean requireSafety();

    static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("for-rollout:ThrowSpecificExceptions")
    static CliConfiguration create(
            String input, String outputIrFile, Map<String, Object> extensions, boolean requireSafety) {
        File inputFile = new File(input);

        Collection<File> inputFiles;
        try {
            inputFiles = resolveInputFiles(inputFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve input files from " + inputFile, e);
        }

        File outputFile = new File(outputIrFile);
        if (outputFile.isDirectory()) {
            throw new RuntimeException("Output IR file should not be a directory: " + outputFile);
        }

        return new Builder()
                .inputFiles(inputFiles)
                .outputIrFile(outputFile)
                .extensions(extensions)
                .requireSafety(requireSafety)
                .build();
    }

    private static Collection<File> resolveInputFiles(File input) throws IOException {
        @SuppressWarnings({"for-rollout:PreferredInterfaceType", "for-rollout:UnnecessaryFinal"})
        final Collection<File> inputFiles;
        if (input.isDirectory()) {
            try (Stream<Path> fileStream = Files.find(input.toPath(), 999, (path, bfa) -> bfa.isRegularFile())) {
                inputFiles = fileStream.map(Path::toFile).collect(Collectors.toList());
            }
        } else if (input.isFile()) {
            inputFiles = ImmutableList.of(input);
        } else {
            throw new IOException("Input is not an existing file or directory: " + input);
        }
        return inputFiles;
    }

    public static final class Builder extends ImmutableCliConfiguration.Builder {}
}

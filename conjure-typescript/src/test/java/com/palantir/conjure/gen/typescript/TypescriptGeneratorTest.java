/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;

@ConjureSubfolderRunner.ParentFolder("src/test/resources")
@RunWith(ConjureSubfolderRunner.class)
public final class TypescriptGeneratorTest {

    private static final ConjureTypeScriptClientGenerator generator = new ConjureTypeScriptClientGenerator(
            new DefaultServiceGenerator(),
            new DefaultTypeGenerator());

    @Test
    public void assertThatFilesRenderAsExpected(Path folder) throws IOException {
        Path expected = folder.resolve("expected");
        Path actual = folder.resolve("actual");

        List<ConjureDefinition> definitions = getInputDefinitions(folder);
        maybeResetExpectedDirectory(expected, definitions);

        clearDirectory(actual);
        generator.emit(definitions, "0.0.0", actual.toFile());
        assertFoldersEqual(expected, actual);
    }

    private void maybeResetExpectedDirectory(Path expected, List<ConjureDefinition> definitions) throws IOException {
        if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))
                || !expected.toFile().isDirectory()) {
            clearDirectory(expected);
            generator.emit(definitions, "0.0.0", expected.toFile());
        }
    }

    private List<ConjureDefinition> getInputDefinitions(Path folder) throws IOException {
        Files.createDirectories(folder);
        List<ConjureDefinition> definitions = Files.walk(folder)
                .map(Path::toFile)
                .filter(file -> file.toString().endsWith(".yml"))
                .map(Conjure::parse)
                .collect(Collectors.toList());

        if (definitions.isEmpty()) {
            throw new RuntimeException(
                    folder + " contains no conjure.yml files, please write one to set up a new test");
        }
        return definitions;
    }

    private void assertFoldersEqual(Path expected, Path actual) throws IOException {
        long count = Files.walk(expected)
                .filter(path -> path.toFile().isFile())
                .map(path -> expected.relativize(path))
                .peek(path -> assertThat(actual.resolve(path)).hasSameContentAs(expected.resolve(path)))
                .count();
        System.out.println(count + " files checked");
    }

    private void clearDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
        Files.walk(directory).filter(path -> path.toFile().isFile()).forEach(path -> path.toFile().delete());
        Files.walk(directory).forEach(path -> path.toFile().delete());
        Files.createDirectories(directory);
    }
}

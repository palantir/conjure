/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.Settings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class BeanJavaTypeGeneratorTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBeanJavaTypeGenerator_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/example-types.yml"));

        List<Path> files = new BeanGenerator(Settings.builder().ignoreUnknownProperties(true).build())
                .emit(def, null, folder.getRoot());

        assertThatFilesAreTheSame(files);
    }

    @Test
    public void testEnumJavaGenerator_withNoUnknown() throws IOException {
        ConjureDefinition def = Conjure.parse(
                "types:\n"
                        + "  definitions:\n"
                        + "    default-package: test.api\n"
                        + "    objects:\n"
                        + "      BareEnumExample:\n"
                        + "        docs: An enum that's just an enum.\n"
                        + "        values:\n"
                        + "          - ONE\n"
                        + "          - TWO\n");

        List<Path> files = new BeanGenerator(Settings.builder().supportUnknownEnumValues(false).build())
                .emit(def, null, folder.getRoot());

        assertThatFilesAreTheSame(files);
    }

    private void assertThatFilesAreTheSame(List<Path> files) throws IOException {
        for (Path file : files) {
            Path expectedFile = Paths.get("src/integrationInput/java/test/api/", file.getFileName().toString());
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                // help make shrink-wrapping output sane
                Files.createDirectories(expectedFile.getParent());
                Files.delete(expectedFile);
                Files.copy(file, expectedFile);
            }
            assertThat(file).hasSameContentAs(expectedFile);
        }
    }
}

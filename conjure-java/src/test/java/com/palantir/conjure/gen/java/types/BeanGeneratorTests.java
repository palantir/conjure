/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.ExperimentalFeatures;
import com.palantir.conjure.gen.java.Settings;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class BeanGeneratorTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBeanGenerator_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-types.yml"));
        List<Path> files = new BeanGenerator(
                Settings.builder().ignoreUnknownProperties(true).build(),
                ImmutableSet.of(ExperimentalFeatures.UnionTypes))
                .emit(def, folder.getRoot());

        assertThatFilesAreTheSame(files);
    }

    @Test
    public void testEnumGenerator_withNoUnknown() throws IOException {
        File conjureFile = writeToTempFile(
                "types:\n"
                        + "  definitions:\n"
                        + "    default-package: test.api\n"
                        + "    objects:\n"
                        + "      BareEnumExample:\n"
                        + "        docs: An enum that's just an enum.\n"
                        + "        values:\n"
                        + "          - ONE\n"
                        + "          - TWO\n");

        ConjureDefinition def = Conjure.parse(conjureFile);
        List<Path> files = new BeanGenerator(Settings.builder().supportUnknownEnumValues(false).build())
                .emit(def, folder.getRoot());

        assertThatFilesAreTheSame(files);
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        File src = folder.newFolder("src");
        Settings settings = Settings.standard();
        BeanGenerator generator = new BeanGenerator(settings);
        generator.emit(conjure, src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "test/api/with/imports/ComplexObjectWithImports.java"))
                .contains("import test.api.StringExample;");

        // Imported files are not generated.
        assertThat(new File(src, "com/palantir/foundry/catalog/api/datasets/BackingFileSystem.java"))
                .doesNotExist();
        assertThat(new File(src, "test/api/StringExample.java")).doesNotExist();
    }

    private void assertThatFilesAreTheSame(List<Path> files) throws IOException {
        for (Path file : files) {
            Path expectedFile = Paths.get("src/integrationInput/java/test/api/", file.getFileName().toString());
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                // help make shrink-wrapping output sane
                Files.createDirectories(expectedFile.getParent());
                Files.deleteIfExists(expectedFile);
                Files.copy(file, expectedFile);
            }
            assertThat(file).hasSameContentAs(expectedFile);
        }
    }

    private File writeToTempFile(String content) {
        try {
            File file = folder.newFile();
            com.google.common.io.Files.write(content, file, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return new String(Files.readAllBytes(Paths.get(srcDir.getPath(), clazz)), StandardCharsets.UTF_8);
    }
}

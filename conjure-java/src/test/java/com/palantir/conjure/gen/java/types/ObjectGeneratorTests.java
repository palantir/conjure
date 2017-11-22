/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
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

public final class ObjectGeneratorTests {

    private static final String REFERENCE_FILES_FOLDER = "src/integrationInput/java";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testObjectGenerator_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-types.yml"));
        List<Path> files = new ObjectGenerator(
                Settings.builder().ignoreUnknownProperties(true).build(),
                ImmutableSet.of(ExperimentalFeatures.DangerousGothamSerializableBeans))
                .emit(def, folder.getRoot());

        assertThatFilesAreTheSame(files, REFERENCE_FILES_FOLDER);
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
        List<Path> files = new ObjectGenerator(Settings.builder().supportUnknownEnumValues(false).build())
                .emit(def, folder.getRoot());

        assertThatFilesAreTheSame(files, REFERENCE_FILES_FOLDER);
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        File src = folder.newFolder("src");
        Settings settings = Settings.standard();
        ObjectGenerator generator = new ObjectGenerator(settings);
        generator.emit(conjure, src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "test/api/with/imports/ComplexObjectWithImports.java"))
                .contains("import com.palantir.product.StringExample;");

        // Imported files are not generated.
        assertThat(new File(src, "com/palantir/foundry/catalog/api/datasets/BackingFileSystem.java"))
                .doesNotExist();
        assertThat(new File(src, "test/api/StringExample.java")).doesNotExist();
    }

    @Test
    public void testConjureErrors() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-errors.yml"));
        List<Path> files = new ObjectGenerator(Settings.standard(), ImmutableSet.of(ExperimentalFeatures.ErrorTypes))
                .emit(def, folder.getRoot());

        assertThatFilesAreTheSame(files, REFERENCE_FILES_FOLDER);
    }

    private void assertThatFilesAreTheSame(List<Path> files, String referenceFilesFolder) throws IOException {
        for (Path file : files) {
            Path relativized = folder.getRoot().toPath().relativize(file);
            Path expectedFile = Paths.get(referenceFilesFolder, relativized.toString());
            if (Boolean.valueOf(System.getProperty("recreate", "false"))) {
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
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return new String(Files.readAllBytes(Paths.get(srcDir.getPath(), clazz)), StandardCharsets.UTF_8);
    }
}

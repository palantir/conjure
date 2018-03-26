/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.dialogue;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.gen.java.Settings;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.types.ObjectGenerator;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class DialogueClientGeneratorTests {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void generatesExpectedCode() throws IOException {
        ConjureDefinition def = Conjure.parse(ImmutableList.of(new File("src/test/resources/" + "service.yml")));
        List<Path> dialogFiles = new DialogClientGenerator().emit(def, folder.getRoot());
        List<Path> objectFiles = new ObjectGenerator(Settings.standard()).emit(def, folder.getRoot());
        // Needed to generate JaxRs for Witchcraft ETE servers
        List<Path> jerseyFiles = new JerseyServiceGenerator(ImmutableSet.of()).emit(def, folder.getRoot());
        assertThatFilesAreTheSame(ImmutableList.<Path>builder()
                        .addAll(dialogFiles).addAll(objectFiles).addAll(jerseyFiles).build(),
                "src/integration/java/");
    }

    private void assertThatFilesAreTheSame(List<Path> files, String referenceFilesFolder) throws IOException {
        for (Path file : files) {
            Path relativized = folder.getRoot().toPath().relativize(file);
            Path expectedFile = Paths.get(referenceFilesFolder, relativized.toString());
            if (Boolean.valueOf(System.getProperty("recreate", "false"))) {
                Files.createDirectories(expectedFile.getParent());
                Files.deleteIfExists(expectedFile);
                Files.copy(file, expectedFile);
            }
            assertThat(file).hasSameContentAs(expectedFile);
        }
    }
}

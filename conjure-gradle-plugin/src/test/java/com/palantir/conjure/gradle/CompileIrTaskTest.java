/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gradle;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CompileIrTaskTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void writeToIr_writes_service_def_in_ir() throws IOException {
        ImmutableSet<File> conjureFiles = ImmutableSet.of(new File("src/test/resources/example-service.yml"));
        verifyWriteToIr(conjureFiles, "service.ir.json");
    }

    @Test
    public void writeToIr_combines_multiple_defs_in_ir() throws IOException {
        ImmutableSet<File> conjureFiles = ImmutableSet.of(
                new File("src/test/resources/example-errors.yml"),
                new File("src/test/resources/example-service.yml"),
                new File("src/test/resources/example-types.yml"));

        verifyWriteToIr(conjureFiles, "combine.ir.json");
    }

    private void verifyWriteToIr(Set<File> sourceFiles, String irFileName) throws IOException {
        File irFile = getIrFile(irFileName);
        CompileIrTask.writeToIr(sourceFiles, irFile);
        assertThat(readFromFile(irFile))
                .isEqualTo(readFromFile(Paths.get("src/test/resources/expected/" + irFileName).toFile()));
    }

    private File getIrFile(String fileName) throws IOException {
        if (Boolean.valueOf(System.getProperty("recreate", "false"))) {
            Path output = Paths.get("src/test/resources/expected/" + fileName);
            Files.deleteIfExists(output);
            return output.toFile();
        } else {
            return Paths.get(folder.getRoot().getAbsolutePath(), fileName).toFile();
        }
    }

    private static String readFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.service;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.Generators;
import com.palantir.conjure.gen.java.Settings;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JerseyServiceGeneratorTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void smokeTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/test-service2.yml"));
        File src = folder.newFolder("src");
        Generators.generateJerseyServices(conjure, Settings.standard(), src);

        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/CreateDatasetRequest.java"))
                .contains("public final class CreateDatasetRequest");
        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/datasets/BackingFileSystem.java"))
                .contains("public final class BackingFileSystem");
        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/datasets/Dataset.java"))
                .contains("public final class Dataset");
        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/TestService.java"))
                .contains("public interface TestService");
    }

    private static String compiledFile(File srcDir, String clazz) throws IOException {
        return Files.asCharSource(new File(srcDir, clazz), StandardCharsets.UTF_8).read();
    }
}

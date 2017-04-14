/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class Retrofit2ServiceGeneratorTests extends TestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testComposition() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/example-service.yml"));

        List<Path> files = new Retrofit2ServiceGenerator().emit(def, folder.getRoot());

        for (Path file : files) {
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                Path output = Paths.get("src/test/resources/test/api/" + file.getFileName() + ".retrofit");
                Files.delete(output);
                Files.copy(file, output);
            }

            assertThat(readFromFile(file)).isEqualTo(readFromResource("/test/api/" + file.getFileName() + ".retrofit"));
        }
    }
}

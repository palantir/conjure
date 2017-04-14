/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JerseyServiceGeneratorTests extends TestBase {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testServiceGeneration_exampleService() throws IOException {
        testServiceGeneration("/example-service.yml");
    }

    @Test
    public void testServiceGeneration_cookieService() throws IOException {
        testServiceGeneration("/cookie-service.yml");
    }

    private void testServiceGeneration(String resource) throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream(resource));
        List<Path> files = new JerseyServiceGenerator().emit(def, folder.getRoot());

        for (Path file : files) {
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                Path output = Paths.get("src/test/resources/test/api/" + file.getFileName() + ".jersey");
                Files.delete(output);
                Files.copy(file, output);
            }

            assertThat(readFromFile(file)).isEqualTo(readFromResource("/test/api/" + file.getFileName() + ".jersey"));
        }
    }
}

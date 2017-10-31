/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
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

public final class Retrofit2ServiceGeneratorTests extends TestBase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCompositionVanilla() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-service.yml"));

        List<Path> files = new Retrofit2ServiceGenerator(ImmutableSet.of()).emit(def, folder.getRoot());

        for (Path file : files) {
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                Path output = Paths.get("src/test/resources/test/api/" + file.getFileName() + ".retrofit");
                Files.delete(output);
                Files.copy(file, output);
            }

            assertThat(readFromFile(file)).isEqualTo(readFromResource("/test/api/" + file.getFileName() + ".retrofit"));
        }
    }

    @Test
    public void testCompositionCompletableFuture() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-service.yml"));

        List<Path> files = new Retrofit2ServiceGenerator(
                ImmutableSet.of(ExperimentalFeatures.RetrofitCompletableFutures))
                .emit(def, folder.getRoot());

        for (Path file : files) {
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                Path output = Paths.get("src/test/resources/test/api/"
                        + file.getFileName() + ".retrofit_completable_future");
                Files.delete(output);
                Files.copy(file, output);
            }

            assertThat(readFromFile(file)).isEqualTo(
                    readFromResource("/test/api/" + file.getFileName() + ".retrofit_completable_future"));
        }
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        File src = folder.newFolder("src");
        Retrofit2ServiceGenerator generator = new Retrofit2ServiceGenerator(ImmutableSet.of());
        generator.emit(conjure, src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "test/api/with/imports/TestService.java"))
                .contains("import test.api.StringExample;");
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return new String(Files.readAllBytes(Paths.get(srcDir.getPath(), clazz)), StandardCharsets.UTF_8);
    }

}

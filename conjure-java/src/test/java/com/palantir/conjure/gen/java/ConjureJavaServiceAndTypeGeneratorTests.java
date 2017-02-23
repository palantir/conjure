/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.TypesDefinition;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.gen.java.types.BeanGenerator;
import com.palantir.conjure.gen.java.types.TypeGenerator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class ConjureJavaServiceAndTypeGeneratorTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testComposition() throws IOException {
        ServiceGenerator serviceGenerator = mock(ServiceGenerator.class);
        TypeGenerator typeGenerator = mock(TypeGenerator.class);
        ConjureJavaServiceAndTypeGenerator generator =
                new ConjureJavaServiceAndTypeGenerator(serviceGenerator, typeGenerator, null);

        TypesDefinition types = mock(TypesDefinition.class);
        ConjureDefinition conjureDefinition = ConjureDefinition.builder()
                .types(types)
                .build();

        generator.generate(conjureDefinition);
        verify(serviceGenerator).generate(conjureDefinition, null);
        verify(typeGenerator).generate(conjureDefinition, null);

        File outputDir = folder.newFolder();
        generator.emit(conjureDefinition, outputDir);
        verify(serviceGenerator).emit(conjureDefinition, null, outputDir);
        verify(typeGenerator).emit(conjureDefinition, null, outputDir);
    }

    @Test
    public void smokeTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-service.yml"));
        File src = folder.newFolder("src");
        Settings settings = Settings.standard();
        ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                new JerseyServiceGenerator(settings),
                new BeanGenerator(settings), null);
        generator.emit(conjure, src);

        assertThat(compiledFileContent(src, "com/palantir/foundry/catalog/api/CreateDatasetRequest.java"))
                .contains("public final class CreateDatasetRequest");
        assertThat(compiledFileContent(src, "com/palantir/foundry/catalog/api/datasets/BackingFileSystem.java"))
                .contains("public final class BackingFileSystem");
        assertThat(compiledFileContent(src, "com/palantir/foundry/catalog/api/datasets/Dataset.java"))
                .contains("public final class Dataset");
        assertThat(compiledFileContent(src, "test/api/TestService.java"))
                .contains("public interface TestService");
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        File src = folder.newFolder("src");
        Settings settings = Settings.standard();
        ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                new JerseyServiceGenerator(settings),
                new BeanGenerator(settings), Paths.get("src/test"));
        generator.emit(conjure, src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "test/api/with/imports/ComplexObjectWithImports.java"))
                .contains("import test.api.StringExample;");
        assertThat(compiledFileContent(src, "test/api/with/imports/TestService.java"))
                .contains("import test.api.StringExample;")
                .contains("import com.palantir.foundry.catalog.api.datasets.BackingFileSystem;");

        // Imported files are not generated.
        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/datasets/BackingFileSystem.java"))
                .doesNotExist();
        assertThat(compiledFile(src, "test/api/StringExample.java")).doesNotExist();
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return Files.asCharSource(compiledFile(srcDir, clazz), StandardCharsets.UTF_8).read();
    }

    private static File compiledFile(File srcDir, String clazz) throws IOException {
        return new File(srcDir, clazz);
    }
}

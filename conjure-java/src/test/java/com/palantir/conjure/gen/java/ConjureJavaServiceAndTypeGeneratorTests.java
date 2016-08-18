/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static com.google.common.truth.Truth.assertThat;
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
                new ConjureJavaServiceAndTypeGenerator(serviceGenerator, typeGenerator);

        TypesDefinition types = mock(TypesDefinition.class);
        ConjureDefinition conjureDefinition = ConjureDefinition.builder()
                .types(types)
                .build();

        generator.generate(conjureDefinition);
        verify(serviceGenerator).generate(conjureDefinition);
        verify(typeGenerator).generate(types);

        File outputDir = folder.newFolder();
        generator.emit(conjureDefinition, outputDir);
        verify(serviceGenerator).emit(conjureDefinition, outputDir);
        verify(typeGenerator).emit(types, outputDir);
    }

    @Test
    public void smokeTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-service.yml"));
        File src = folder.newFolder("src");
        Settings settings = Settings.standard();
        ConjureJavaServiceAndTypeGenerator generator = new ConjureJavaServiceAndTypeGenerator(
                new JerseyServiceGenerator(settings),
                new BeanGenerator(settings));
        generator.emit(conjure, src);

        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/CreateDatasetRequest.java"))
                .contains("public final class CreateDatasetRequest");
        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/datasets/BackingFileSystem.java"))
                .contains("public final class BackingFileSystem");
        assertThat(compiledFile(src, "com/palantir/foundry/catalog/api/datasets/Dataset.java"))
                .contains("public final class Dataset");
        assertThat(compiledFile(src, "test/api/TestService.java"))
                .contains("public interface TestService");
    }

    private static String compiledFile(File srcDir, String clazz) throws IOException {
        return Files.asCharSource(new File(srcDir, clazz), StandardCharsets.UTF_8).read();
    }
}

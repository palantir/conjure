/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.python;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.python.client.ClientGenerator;
import com.palantir.conjure.gen.python.poet.PythonFile;
import com.palantir.conjure.gen.python.poet.PythonPoetWriter;
import com.palantir.conjure.gen.python.types.DefaultBeanGenerator;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class ConjurePythonGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testGenerateTypes() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-types.yml"));
        ConjurePythonGenerator generator = new ConjurePythonGenerator(
                new DefaultBeanGenerator(), new ClientGenerator());

        InMemoryPythonFileWriter pythonFileWriter = new InMemoryPythonFileWriter();
        generator.write(conjure, pythonFileWriter);

        Path referenceCodeLocation = Paths.get("src/test/resources/python/example-types");

        pythonFileWriter.getPythonFiles().forEach((path, generatedCode) -> {
            try {
                String contents = Files.asCharSource(
                        referenceCodeLocation.resolve(path).toAbsolutePath().toFile(), StandardCharsets.UTF_8).read();
                assertThat(generatedCode).isEqualTo(contents);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testGenerateService() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-service.yml"));
        ConjurePythonGenerator generator = new ConjurePythonGenerator(
                new DefaultBeanGenerator(), new ClientGenerator());

        InMemoryPythonFileWriter pythonFileWriter = new InMemoryPythonFileWriter();
        generator.write(conjure, pythonFileWriter);

        Path referenceCodeLocation = Paths.get("src/test/resources/python/example-service");

        pythonFileWriter.getPythonFiles().forEach((path, generatedCode) -> {
            try {
                String contents = Files.asCharSource(
                        referenceCodeLocation.resolve(path).toAbsolutePath().toFile(), StandardCharsets.UTF_8).read();
                assertThat(generatedCode).isEqualTo(contents);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static class InMemoryPythonFileWriter implements PythonFileWriter {

        private final Map<Path, String> pythonFiles;

        InMemoryPythonFileWriter() {
            this.pythonFiles = Maps.newHashMap();
        }

        public Map<Path, String> getPythonFiles() {
            return pythonFiles;
        }

        @Override
        public void writePythonFile(PythonFile pythonFile) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream printStream = new PrintStream(baos)) {
                PythonPoetWriter poetWriter = new PythonPoetWriter(printStream);

                poetWriter.emit(pythonFile);
                byte[] bytes = baos.toByteArray();

                pythonFiles.put(PythonFileWriter.getPath(pythonFile), new String(bytes, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> conjureFiles = ImmutableList.of(
                "src/test/resources/example-types.yml",
                "src/test/resources/example-service.yml");
        for (String conjureFile : conjureFiles) {
            File file = new File(conjureFile);
            ConjureDefinition conjure = Conjure.parse(file);

            Path referenceCodeLocation = Paths.get("src/test/resources/python/").resolve(
                    file.getName().replaceAll(".yml$", ""));
            Files.createParentDirs(referenceCodeLocation.toFile());
            ConjurePythonGenerator generator = new ConjurePythonGenerator(
                    new DefaultBeanGenerator(), new ClientGenerator());
            generator.write(conjure, new DefaultPythonFileWriter(referenceCodeLocation));
        }

    }

}

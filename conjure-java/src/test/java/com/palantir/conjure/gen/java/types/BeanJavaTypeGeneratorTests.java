/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import org.junit.Test;

public final class BeanJavaTypeGeneratorTests {

    @Test
    public void testBeanJavaTypeGenerator_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/example-types.yml"));

        Set<JavaFile> files = new BeanGenerator(Settings.builder().ignoreUnknownProperties(true).build())
                .generate(def.types());

        assertThatFilesAreTheSame(files);
    }

    @Test
    public void testEnumJavaGenerator_withNoUnknown() throws IOException {
        ConjureDefinition def = Conjure.parse(
                "types:\n"
                + "  definitions:\n"
                + "    default-package: test.api\n"
                + "    objects:\n"
                + "      BareEnumExample:\n"
                + "        docs: An enum that's just an enum.\n"
                + "        values:\n"
                + "          - ONE\n"
                + "          - TWO\n");

        Set<JavaFile> files = new BeanGenerator(Settings.builder().supportUnknownEnumValues(false).build())
                .generate(def.types());

        assertThatFilesAreTheSame(files);
    }

    private void assertThatFilesAreTheSame(Set<JavaFile> files) throws IOException {
        for (JavaFile file : files) {
            File expectedFile = new File("src/test/resources/test/api/" + file.typeSpec.name + ".bean");
            if (Boolean.valueOf(System.getProperty("NOT_SAFE_FOR_CI", "false"))) {
                // help make shrink-wrapping output sane
                if (!expectedFile.exists()) {
                    Files.write(expectedFile.toPath(), file.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
            assertThat(file.toString())
                    .isEqualTo(new String(Files.readAllBytes(expectedFile.toPath()), StandardCharsets.UTF_8));
        }
    }
}

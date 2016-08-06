/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.Settings;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.Test;

public final class BeanJavaTypeGeneratorTests {

    @Test
    public void testImmutablesGenerator_normalCase() throws IOException {
        testGeneratedObjectMatchesExpectation("SimpleObject");
    }

    @Test
    public void testImmutablesGenerator_kebabCase() throws IOException {
        testGeneratedObjectMatchesExpectation("KebabCaseObject");
    }

    private void testGeneratedObjectMatchesExpectation(String testCase) throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/" + testCase + ".conjure"));

        Set<JavaFile> files = new BeanJavaTypeGenerator(Settings.builder().ignoreUnknownProperties(true).build())
                .generate(def.types());

        assertThat(files.size()).isEqualTo(1);
        assertThat(Iterables.getFirst(files, null).toString())
                .isEqualTo(CharStreams.toString(
                        new InputStreamReader(getClass().getResourceAsStream("/" + testCase + ".java.bean"),
                                StandardCharsets.UTF_8)));
    }

}

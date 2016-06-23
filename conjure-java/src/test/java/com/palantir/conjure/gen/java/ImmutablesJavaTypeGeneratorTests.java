/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.types.ImmutablesJavaTypeGenerator;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.Test;

public final class ImmutablesJavaTypeGeneratorTests {

    @Test
    public void testImmutablesGenerator() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/SimpleObject.conjure"));

        Set<JavaFile> files = new ImmutablesJavaTypeGenerator()
                .generate(def.types(), Settings.builder().ignoreUnknownProperties(true).build());

        assertThat(files.size()).isEqualTo(1);
        assertThat(Iterables.getFirst(files, null).toString())
                .isEqualTo(CharStreams.toString(
                        new InputStreamReader(getClass().getResourceAsStream("/SimpleObject.java.sample"),
                                StandardCharsets.UTF_8)));
    }

}

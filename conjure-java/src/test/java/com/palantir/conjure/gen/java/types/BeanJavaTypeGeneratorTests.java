/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static com.google.common.truth.Truth.assertThat;

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
    public void testBeanJavaTypeGenerator_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/example-types.yml"));

        Set<JavaFile> files = new BeanGenerator(Settings.builder().ignoreUnknownProperties(true).build())
                .generate(def.types());

        for (JavaFile file : files) {
            assertThat(file.toString()).isEqualTo(CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream("/test/api/" + file.typeSpec.name + ".bean"),
                            StandardCharsets.UTF_8)));
        }
    }

}

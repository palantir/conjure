/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.Test;

public final class Retrofit2ServiceGeneratorTests {
    @Test
    public void testComposition() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/example-service.yml"));

        ServiceGenerator gen = new Retrofit2ServiceGenerator();

        Set<JavaFile> files = gen.generate(def, null);

        for (JavaFile file : files) {
            assertThat(file.toString()).isEqualTo(CharStreams.toString(
                    new InputStreamReader(
                            getClass().getResourceAsStream("/test/api/" + file.typeSpec.name + ".retrofit"),
                            StandardCharsets.UTF_8)));
        }

    }
}

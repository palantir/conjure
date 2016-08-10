/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.Retrofit2ServiceGenerator;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.gen.java.types.TypeMapper.OptionalTypeStrategy;
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

        ServiceGenerator gen = new Retrofit2ServiceGenerator(Settings.builder()
                .ignoreUnknownProperties(false)
                .optionalTypeStrategy(OptionalTypeStrategy.Java8)
                .build());

        Set<JavaFile> files = gen.generate(def);

        for (JavaFile file : files) {
            assertThat(file.toString()).isEqualTo(CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream("/test/api/" + file.typeSpec.name + ".serv"),
                            StandardCharsets.UTF_8)));
        }

    }
}

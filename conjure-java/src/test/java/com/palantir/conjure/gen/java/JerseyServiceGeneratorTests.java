/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import com.palantir.conjure.gen.java.services.ServiceGenerator;
import com.palantir.conjure.gen.java.types.TypeMapper.OptionalTypeStrategy;
import com.squareup.javapoet.JavaFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.Test;

public final class JerseyServiceGeneratorTests {
    @Test
    public void testServiceGeneration_exampleService() throws IOException {
        testServiceGeneration("/example-service.yml");
    }

    @Test
    public void testServiceGeneration_cookieService() throws IOException {
        testServiceGeneration("/cookie-service.yml");
    }

    private void testServiceGeneration(String resource) throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream(resource));

        ServiceGenerator gen = new JerseyServiceGenerator(Settings.builder()
                .ignoreUnknownProperties(false)
                .optionalTypeStrategy(OptionalTypeStrategy.JAVA8)
                .build());

        Set<JavaFile> files = gen.generate(def);

        for (JavaFile file : files) {
            assertThat(file.toString()).isEqualTo(CharStreams.toString(
                    new InputStreamReader(
                            getClass().getResourceAsStream("/test/api/" + file.typeSpec.name + ".jersey"),
                            StandardCharsets.UTF_8)));
        }
    }
}

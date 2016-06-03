/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.server;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public final class JerseyServiceGeneratorTests {

    @Test
    public void smokeTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/test-service2.yml"));
        JerseyServiceGenerator gen = new JerseyServiceGenerator(conjure.types());
        gen.generateTypes().forEach(s -> {
            System.out.println("----------");
            System.out.println(s);
        });

        gen.generateServices(conjure.services()).forEach(s -> {
            System.out.println("----------");
            System.out.println(s);
        });
    }

}

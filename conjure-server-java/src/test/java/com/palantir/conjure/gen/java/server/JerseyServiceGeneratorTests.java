/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.server;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JerseyServiceGeneratorTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void smokeTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/test-service2.yml"));
        JerseyServiceGenerator gen = new JerseyServiceGenerator(conjure);
        File src = folder.newFolder("src");
        gen.emit(src);
    }

}

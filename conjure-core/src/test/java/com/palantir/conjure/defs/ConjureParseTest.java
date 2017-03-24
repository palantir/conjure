/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

public final class ConjureParseTest {

    @Test
    public void testParseValidSpecs() throws IOException {
        File[] files = new File("src/test/resources/valid").listFiles();
        Arrays.sort(files);
        for (File currFile : files) {
            try {
                Conjure.parse(currFile);
            } catch (RuntimeException e) {
                fail("Failed to parse Conjure YML file: " + currFile.getPath(), e);
            }
        }
    }

}

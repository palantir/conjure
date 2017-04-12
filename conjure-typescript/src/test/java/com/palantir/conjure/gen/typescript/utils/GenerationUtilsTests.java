/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.palantir.conjure.defs.types.ConjurePackage;
import org.junit.Test;

public final class GenerationUtilsTests {

    @Test
    public void relativePathsTest() {
        assertEquals("./b", GenerationUtils.getRelativePath("a", "b"));
        assertEquals("./b", GenerationUtils.getRelativePath("x/a", "x/b"));
        assertEquals("./x/b", GenerationUtils.getRelativePath("y/a", "y/x/b"));
        assertEquals("../b", GenerationUtils.getRelativePath("x/a", "b"));
        assertEquals("../../b", GenerationUtils.getRelativePath("x/y/a", "b"));
        assertEquals("../y/b", GenerationUtils.getRelativePath("x/a", "y/b"));
    }

    @Test
    public void packageToFolderPathTest() {
        assertEquals("c", GenerationUtils.packageToFolderPath(ConjurePackage.of("a1.b.c")));
        assertEquals("c/d", GenerationUtils.packageToFolderPath(ConjurePackage.of("a1.b.c.d")));
        assertEquals("c/d/e", GenerationUtils.packageToFolderPath(ConjurePackage.of("a1.b.c.d.e")));
        try {
            GenerationUtils.packageToFolderPath(ConjurePackage.of("a1.b"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("at least 3");
        }
    }
}

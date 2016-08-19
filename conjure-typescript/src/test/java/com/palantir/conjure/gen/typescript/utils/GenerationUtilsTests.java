/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import static org.junit.Assert.assertEquals;

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
}

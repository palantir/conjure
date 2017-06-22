/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.defs.types.names.ConjurePackage;
import org.junit.Test;

public final class GenerationUtilsTests {

    @Test
    public void packageToScopeandModuleTest() {
        assertThat(GenerationUtils.packageToScopeAndModule(ConjurePackage.of("a1.b.c"))).isEqualTo("@b/c");
        assertThat(GenerationUtils.packageToScopeAndModule(ConjurePackage.of("a1.b.c.d"))).isEqualTo("@b/c-d");
        assertThatThrownBy(() -> GenerationUtils.packageToScopeAndModule(ConjurePackage.of("a1.b")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("packages should have at least 3 segments");
    }
}

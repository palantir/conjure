/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public final class ParameterNameTest {
    @Test
    public void testValidPackageNames() {
        ParameterName.of("f");
        ParameterName.of("foo");
        ParameterName.of("fooBar");
        ParameterName.of("fooBar1");
        ParameterName.of("a1Foo234");
    }

    @Test
    public void testInvalidPackageNames() {
        for (String illegal : new String[] {"AB", "123", "foo_bar", "foo-bar", "foo.bar"}) {
            assertThatThrownBy(() -> ParameterName.of(illegal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Parameter names in endpoint paths and service definitions must match pattern "
                            + "^[a-z][a-z0-9]*([A-Z0-9][a-z0-9]+)*$: %s", illegal);
        }
    }

}

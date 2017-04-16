/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public final class ConjurePackageTest {

    @Test
    public void testValidPackageNames() {
        ConjurePackage.of("foo");
        ConjurePackage.of("foo.bar");
        ConjurePackage.of("foo.bar.baz");
        ConjurePackage.of("ab.c.d");
        ConjurePackage.of("a1.b2.c3");
    }

    @Test
    public void testInvalidPackageNames() {
        for (String illegal : new String[] {".", "foo-bar", "foo_bar", "1a", "a.foo"}) {
            assertThatThrownBy(() -> ConjurePackage.of(illegal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Conjure package names must match pattern ^([a-z][a-z0-9]+(\\.[a-z][a-z0-9]*)*)?$: "
                            + illegal);
        }
    }

    @Test
    public void testComponents() throws Exception {
        assertThat(ConjurePackage.of("foo.bar.baz").components()).containsExactly("foo", "bar", "baz");
        assertThat(ConjurePackage.of(ImmutableList.of("foo", "bar", "baz")))
                .isEqualTo(ConjurePackage.of("foo.bar.baz"));
    }
}

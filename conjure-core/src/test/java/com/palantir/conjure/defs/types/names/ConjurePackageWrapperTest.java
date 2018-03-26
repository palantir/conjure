/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Test;

public final class ConjurePackageWrapperTest {

    @Test
    public void testValidPackageNames() {
        ConjurePackageWrapper.validate("foo");
        ConjurePackageWrapper.validate("foo.bar");
        ConjurePackageWrapper.validate("foo.bar.baz");
        ConjurePackageWrapper.validate("ab.c.d");
        ConjurePackageWrapper.validate("a1.b2.c3");
    }

    @Test
    public void testInvalidPackageNames() {
        for (String illegal : new String[] {".", "foo-bar", "foo_bar", "1a", "a.foo"}) {
            assertThatThrownBy(() -> ConjurePackageWrapper.validate(illegal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Conjure package names must match pattern ^([a-z][a-z0-9]+(\\.[a-z][a-z0-9]*)*)?$: "
                            + illegal);
        }
    }

    @Test
    public void testComponents() {
        assertThat(ConjurePackageWrapper.components("foo.bar.baz")).containsExactly("foo", "bar", "baz");
    }
}

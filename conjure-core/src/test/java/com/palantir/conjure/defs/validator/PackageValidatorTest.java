/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Test;

public final class PackageValidatorTest {

    @Test
    public void testValidPackageNames() {
        PackageValidator.validate("foo");
        PackageValidator.validate("foo.bar");
        PackageValidator.validate("foo.bar.baz");
        PackageValidator.validate("ab.c.d");
        PackageValidator.validate("a1.b2.c3");
    }

    @Test
    public void testInvalidPackageNames() {
        for (String illegal : new String[] {".", "foo-bar", "foo_bar", "1a", "a.foo"}) {
            assertThatThrownBy(() -> PackageValidator.validate(illegal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Conjure package names must match pattern ^([a-z][a-z0-9]+(\\.[a-z][a-z0-9]*)*)?$: "
                            + illegal);
        }
    }

    @Test
    public void testComponents() {
        assertThat(PackageValidator.components("foo.bar.baz")).containsExactly("foo", "bar", "baz");
    }
}

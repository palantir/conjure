/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

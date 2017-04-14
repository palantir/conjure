/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public final class NamespaceTest {

    @Test
    public void testValidNameSpaceNames() throws Exception {
        Namespace.of("abc");
        Namespace.of("abcDef");

        for (String ns : new String[] {"a", "ns1", "foo$"}) {
            assertThatThrownBy(() -> Namespace.of(ns))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Namespaces must match pattern ^[a-z][a-z]+([A-Z][a-z]+)*$: %s", ns);
        }
    }
}

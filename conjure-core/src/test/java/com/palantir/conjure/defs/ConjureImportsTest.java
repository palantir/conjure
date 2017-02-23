/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public final class ConjureImportsTest {

    @Test
    public void testValidNameSpaceNames() throws Exception {
        ObjectsDefinition imports = mock(ObjectsDefinition.class);
        new ConjureImports(ImmutableMap.of("ab", imports, "abcdefghij", imports));

        for (String ns : new String[] {"", "a", "ABC", "%s"}) {
            assertThatThrownBy(() -> new ConjureImports(ImmutableMap.of(ns, imports)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The following namespaces do not satisfy the namespace pattern [a-z]{2,10}: [%s]", ns);
        }
    }
}

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
        new ConjureImports(ImmutableMap.of("ab", imports, "camelCase", imports));

        for (String ns : new String[] {"", "ns1", "%$"}) {
            assertThatThrownBy(() -> new ConjureImports(ImmutableMap.of(ns, imports)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The following namespaces do not satisfy the namespace pattern [a-zA-Z]+: [%s]", ns);
        }
    }
}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public final class ErrorNamespaceTest {

    @Test
    public void testValidErrorNamespaces() throws Exception {
        ErrorNamespace.of("Conjure");
        ErrorNamespace.of("FoundryCatalog");
        ErrorNamespace.of("FoundryFsClient");
    }

    @Test
    public void testInvalidErrorNamespaces() {
        for (String invalid : new String[] {
                "conjure",
                "foundryCatalog",
                "foundry-catalog",
                "FOUNDRY-CATALOG",
                "foundry_catalog",
                "FOUNDRY_CATALOG"
        }) {
            assertThatThrownBy(() -> ErrorNamespace.of(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Namespace for errors must match pattern")
                    .hasMessageContaining(invalid);
        }
    }

}

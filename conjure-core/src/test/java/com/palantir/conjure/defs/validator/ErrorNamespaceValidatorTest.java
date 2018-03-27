/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.ErrorNamespace;
import org.junit.Test;

public final class ErrorNamespaceValidatorTest {

    @Test
    public void testValidErrorNamespaces() throws Exception {
        ErrorNamespaceValidator.validate(ErrorNamespace.of("Conjure"));
        ErrorNamespaceValidator.validate(ErrorNamespace.of("FoundryCatalog"));
        ErrorNamespaceValidator.validate(ErrorNamespace.of("FoundryFsClient"));
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
            assertThatThrownBy(() -> ErrorNamespaceValidator.validate(ErrorNamespace.of(invalid)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Namespace for errors must match pattern")
                    .hasMessageContaining(invalid);
        }
    }

}

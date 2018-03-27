/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.spec.TypeName;
import org.junit.Test;

public final class TypeNameValidatorTest {

    private static final String PACKAGE = "package";

    @Test
    public void testValidNames() {
        TypeNameValidator.validate(TypeName.of("Camel", PACKAGE));
        TypeNameValidator.validate(TypeName.of("CamelCase", PACKAGE));
        TypeNameValidator.validate(TypeName.of("CamelCase1", PACKAGE));
        TypeNameValidator.validate(TypeName.of("Camel1Case2", PACKAGE));
    }

    @Test
    public void testInvalidNames() {
        for (String invalid : new String[] {"a", "IFoo", "ABC", "$Special", "snake_case", "kebab-case", "Camel1B"}) {
            assertThatThrownBy(() -> TypeNameValidator.validate(TypeName.of(invalid, PACKAGE)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TypeNames must be a primitive type"
                            + " [STRING, DATETIME, INTEGER, DOUBLE, SAFELONG, BINARY, ANY, BOOLEAN, UUID, RID,"
                            + " BEARERTOKEN, UNKNOWN] or match pattern ^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$: %s",
                            invalid);
        }
    }
}

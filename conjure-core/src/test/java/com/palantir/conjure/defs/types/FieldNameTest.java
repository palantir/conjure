/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public final class FieldNameTest {

    @Test
    public void testValidNames() {
        FieldName.of("camel");
        FieldName.of("camelCase");
        FieldName.of("camelCase1");
        FieldName.of("camel1Case2");

        FieldName.of("kebab-case");
        FieldName.of("kebab1-case123");

        FieldName.of("snake_case");
        FieldName.of("snake1_case123");
    }

    @Test
    public void testInvalidNames() throws Exception {
        for (String invalid : new String[] {
                "UpperCamelCase",
                "Upper-Kebab-Case",
                "Upper_Snake_Case",
                "ABC",
                "$special",
                "special%",
                "snake_123_case",
                "kebab-123-case",
                "mixed-kebab_snake-case",
                }) {
            assertThatThrownBy(() -> FieldName.of(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("FieldNames must be in "
                                    + "lowerCamelCase (^[a-z][a-z0-9]+([A-Z][a-z0-9]+)*$), "
                                    + "kebab-case (^[a-z][a-z0-9]+(-[a-z][a-z0-9]+)*$), or "
                                    + "snake_case (^[a-z][a-z0-9]+(_[a-z][a-z0-9]+)*$): %s",
                            invalid);
        }
    }
}

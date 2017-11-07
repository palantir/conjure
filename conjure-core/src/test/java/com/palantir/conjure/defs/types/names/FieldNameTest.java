/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThat;
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

        FieldName.of("xCoordinate");
        FieldName.of("defaultXPosition");
        FieldName.of("defaultX");
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
                "x",
                "defaultDNSName"
                }) {
            assertThatThrownBy(() -> FieldName.of(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format(
                            "FieldName \"%s\" must follow one of the following patterns",
                            invalid));
        }
    }

    @Test
    public void testConversion() throws Exception {
        FieldName camelCase = FieldName.of("fooBar");
        FieldName kebabCase = FieldName.of("foo-bar");
        FieldName snakeCase = FieldName.of("foo_bar");

        assertThat(camelCase.toCase(FieldName.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(camelCase.toCase(FieldName.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(camelCase.toCase(FieldName.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(kebabCase.toCase(FieldName.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(kebabCase.toCase(FieldName.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(kebabCase.toCase(FieldName.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(snakeCase.toCase(FieldName.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(snakeCase.toCase(FieldName.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(snakeCase.toCase(FieldName.Case.SNAKE_CASE)).isEqualTo(snakeCase);
    }

    @Test
    public void capitalize_should_turn_camel_case_into_sensible_class_name() throws Exception {
        assertThat(FieldName.of("myVariant").capitalize()).isEqualTo("MyVariant");
    }

    @Test
    public void capitalize_capture_unused_behavior() throws Exception {
        assertThat(FieldName.of("my-variant").capitalize()).isEqualTo("My-variant");
        assertThat(FieldName.of("my_variant").capitalize()).isEqualTo("My_variant");
    }
}

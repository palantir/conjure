/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.palantir.conjure.spec.FieldName;
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
            assertThatThrownBy(() -> FieldNameWrapper.fieldName(invalid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format(
                            "FieldName \"%s\" must follow one of the following patterns",
                            invalid));
        }
    }

    @Test
    public void testConversion() throws Exception {
        FieldName camelCase = FieldNameWrapper.fieldName("fooBar");
        FieldName kebabCase = FieldNameWrapper.fieldName("foo-bar");
        FieldName snakeCase = FieldNameWrapper.fieldName("foo_bar");

        assertThat(FieldNameWrapper.toCase(camelCase, FieldNameWrapper.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(FieldNameWrapper.toCase(camelCase, FieldNameWrapper.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(FieldNameWrapper.toCase(camelCase, FieldNameWrapper.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(FieldNameWrapper.toCase(kebabCase, FieldNameWrapper.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(FieldNameWrapper.toCase(kebabCase, FieldNameWrapper.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(FieldNameWrapper.toCase(kebabCase, FieldNameWrapper.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(FieldNameWrapper.toCase(snakeCase, FieldNameWrapper.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(FieldNameWrapper.toCase(snakeCase, FieldNameWrapper.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(FieldNameWrapper.toCase(snakeCase, FieldNameWrapper.Case.SNAKE_CASE)).isEqualTo(snakeCase);
    }

    @Test
    public void capitalize_should_turn_camel_case_into_sensible_class_name() throws Exception {
        assertThat(FieldNameWrapper.capitalize(FieldName.of("myVariant"))).isEqualTo("MyVariant");
    }

    @Test
    public void capitalize_capture_unused_behavior() throws Exception {
        assertThat(FieldNameWrapper.capitalize(FieldName.of("my-variant"))).isEqualTo("My-variant");
        assertThat(FieldNameWrapper.capitalize(FieldName.of("my_variant"))).isEqualTo("My_variant");
    }
}

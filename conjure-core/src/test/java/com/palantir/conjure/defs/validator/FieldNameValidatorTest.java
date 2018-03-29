/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.palantir.conjure.spec.FieldName;
import org.junit.Test;

public final class FieldNameValidatorTest {

    @Test
    public void testValidNames() {
        FieldNameValidator.validate(FieldName.of("camel"));
        FieldNameValidator.validate(FieldName.of("camelCase"));
        FieldNameValidator.validate(FieldName.of("camelCase1"));
        FieldNameValidator.validate(FieldName.of("camel1Case2"));
        FieldNameValidator.validate(FieldName.of("kebab-case"));
        FieldNameValidator.validate(FieldName.of("kebab1-case123"));
        FieldNameValidator.validate(FieldName.of("snake_case"));
        FieldNameValidator.validate(FieldName.of("snake1_case123"));
        FieldNameValidator.validate(FieldName.of("xCoordinate"));
        FieldNameValidator.validate(FieldName.of("defaultXPosition"));
        FieldNameValidator.validate(FieldName.of("defaultX"));
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
            assertThatThrownBy(() -> FieldNameValidator.validate(FieldName.of(invalid)))
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

        assertThat(FieldNameValidator.toCase(camelCase, FieldNameValidator.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(FieldNameValidator.toCase(camelCase, FieldNameValidator.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(FieldNameValidator.toCase(camelCase, FieldNameValidator.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(FieldNameValidator.toCase(kebabCase, FieldNameValidator.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(FieldNameValidator.toCase(kebabCase, FieldNameValidator.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(FieldNameValidator.toCase(kebabCase, FieldNameValidator.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(FieldNameValidator.toCase(snakeCase, FieldNameValidator.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(FieldNameValidator.toCase(snakeCase, FieldNameValidator.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(FieldNameValidator.toCase(snakeCase, FieldNameValidator.Case.SNAKE_CASE)).isEqualTo(snakeCase);
    }

    @Test
    public void capitalize_should_turn_camel_case_into_sensible_class_name() throws Exception {
        assertThat(FieldNameValidator.capitalize(FieldName.of("myVariant"))).isEqualTo("MyVariant");
    }

    @Test
    public void capitalize_capture_unused_behavior() throws Exception {
        assertThat(FieldNameValidator.capitalize(FieldName.of("my-variant"))).isEqualTo("My-variant");
        assertThat(FieldNameValidator.capitalize(FieldName.of("my_variant"))).isEqualTo("My_variant");
    }
}

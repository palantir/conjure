/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CaseConverterTest {

    @Test
    public void convertFromCamelCase() {
        assertThat(CaseConverter.toCase("fooBarBaz", CaseConverter.Case.KEBAB_CASE)).isEqualTo("foo-bar-baz");
        assertThat(CaseConverter.toCase("fooBarBaz", CaseConverter.Case.SNAKE_CASE)).isEqualTo("foo_bar_baz");
    }

    @Test
    public void convertFromKebabCase() {
        assertThat(CaseConverter.toCase("foo-bar-baz", CaseConverter.Case.LOWER_CAMEL_CASE)).isEqualTo("fooBarBaz");
        assertThat(CaseConverter.toCase("foo-bar-baz", CaseConverter.Case.SNAKE_CASE)).isEqualTo("foo_bar_baz");
    }

    @Test
    public void convertFromSnakeCase() {
        assertThat(CaseConverter.toCase("foo_bar_baz", CaseConverter.Case.KEBAB_CASE)).isEqualTo("foo-bar-baz");
        assertThat(CaseConverter.toCase("foo_bar_baz", CaseConverter.Case.LOWER_CAMEL_CASE)).isEqualTo("fooBarBaz");
    }
    @Test
    public void testConversion() throws Exception {
        String camelCase = "fooBar";
        String kebabCase = "foo-bar";
        String snakeCase = "foo_bar";

        assertThat(CaseConverter.toCase(camelCase, CaseConverter.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(CaseConverter.toCase(camelCase, CaseConverter.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(CaseConverter.toCase(camelCase, CaseConverter.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(CaseConverter.toCase(kebabCase, CaseConverter.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(CaseConverter.toCase(kebabCase, CaseConverter.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(CaseConverter.toCase(kebabCase, CaseConverter.Case.SNAKE_CASE)).isEqualTo(snakeCase);

        assertThat(CaseConverter.toCase(snakeCase, CaseConverter.Case.LOWER_CAMEL_CASE)).isEqualTo(camelCase);
        assertThat(CaseConverter.toCase(snakeCase, CaseConverter.Case.KEBAB_CASE)).isEqualTo(kebabCase);
        assertThat(CaseConverter.toCase(snakeCase, CaseConverter.Case.SNAKE_CASE)).isEqualTo(snakeCase);
    }

}

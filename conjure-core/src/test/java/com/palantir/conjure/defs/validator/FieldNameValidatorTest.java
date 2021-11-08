/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.palantir.conjure.spec.FieldName;
import org.junit.jupiter.api.Test;

public final class FieldNameValidatorTest {

    @Test
    public void testValidNames() {
        FieldNameValidator.validate(FieldName.of("camel"), false);
        FieldNameValidator.validate(FieldName.of("camelCase"), false);
        FieldNameValidator.validate(FieldName.of("camelCase1"), false);
        FieldNameValidator.validate(FieldName.of("camel1Case2"), false);
        FieldNameValidator.validate(FieldName.of("kebab-case"), false);
        FieldNameValidator.validate(FieldName.of("kebab1-case123"), false);
        FieldNameValidator.validate(FieldName.of("snake_case"), false);
        FieldNameValidator.validate(FieldName.of("snake1_case123"), false);
        FieldNameValidator.validate(FieldName.of("xCoordinate"), false);
        FieldNameValidator.validate(FieldName.of("defaultXPosition"), false);
        FieldNameValidator.validate(FieldName.of("defaultX"), false);
    }

    @Test
    public void testValidNames_strict() {
        FieldNameValidator.validate(FieldName.of("camel"), true);
        FieldNameValidator.validate(FieldName.of("camelCase"), true);
        FieldNameValidator.validate(FieldName.of("camelCase1"), true);
        FieldNameValidator.validate(FieldName.of("camel1Case2"), true);
        assertThatThrownBy(() -> FieldNameValidator.validate(FieldName.of("kebab-case"), true))
                .hasMessageContaining("lowerCamelCase");
        assertThatThrownBy(() -> FieldNameValidator.validate(FieldName.of("kebab1-case123"), true))
                .hasMessageContaining("lowerCamelCase");
        assertThatThrownBy(() -> FieldNameValidator.validate(FieldName.of("snake_case"), true))
                .hasMessageContaining("lowerCamelCase");
        assertThatThrownBy(() -> FieldNameValidator.validate(FieldName.of("snake1_case123"), true))
                .hasMessageContaining("lowerCamelCase");
        FieldNameValidator.validate(FieldName.of("xCoordinate"), true);
        FieldNameValidator.validate(FieldName.of("defaultXPosition"), true);
        FieldNameValidator.validate(FieldName.of("defaultX"), true);
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
            "defaultDNSName"
        }) {
            assertThatThrownBy(() -> FieldNameValidator.validate(FieldName.of(invalid)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            String.format("FieldName \"%s\" must follow one of the following patterns", invalid));
        }
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

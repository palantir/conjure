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

import com.palantir.conjure.spec.EnumValueDefinition;
import org.junit.jupiter.api.Test;

public final class EnumValueDefinitionValidatorTest {

    @Test
    public void testUnknownValueNotUsed() {
        for (String value : new String[] {"UNKNOWN", "Unknown"}) {
            assertThatThrownBy(() -> EnumValueDefinitionValidator.validateAll(
                            EnumValueDefinition.builder().value(value).build()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("UNKNOWN is a reserved enumeration value and cannot be used in an EnumValueDefinition");
        }
    }

    @Test
    public void testFormat() {
        EnumValueDefinitionValidator.validateAll(
                EnumValueDefinition.builder().value("FOO").build());
        EnumValueDefinitionValidator.validateAll(
                EnumValueDefinition.builder().value("FOO_BAR").build());
        EnumValueDefinitionValidator.validateAll(
                EnumValueDefinition.builder().value("FOO_123_BAR").build());
        EnumValueDefinitionValidator.validateAll(
                EnumValueDefinition.builder().value("F12").build());

        for (String value :
                new String[] {"foo", "fooBar", "FOO-BAR", " - a", " - a_b", " - A__B", " - _A", " - A_", "123_FOO"}) {
            assertThatThrownBy(() -> EnumValueDefinitionValidator.validateAll(
                            EnumValueDefinition.builder().value(value).build()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Enumeration values must match format [A-Z][A-Z0-9]*(_[A-Z0-9]+)*: %s", value);
        }
    }
}

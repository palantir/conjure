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

import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.conjure.spec.TypeName;
import org.junit.jupiter.api.Test;

public final class EnumDefinitionValidatorTest {

    @Test
    public void testUniqueEnumValues() {
        EnumDefinition.Builder definition = EnumDefinition.builder()
                .typeName(TypeName.of("Foo", "package"))
                .values(EnumValueDefinition.builder().value("FOO").build())
                .values(EnumValueDefinition.builder().value("FOO").build());

        assertThatThrownBy(() -> EnumDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot declare a EnumTypeDefinition with duplicate enum values: FOO");
    }
}

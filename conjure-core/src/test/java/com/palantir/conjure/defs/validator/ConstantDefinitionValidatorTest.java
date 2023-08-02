/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.spec.ConstantDefinition;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.TypeName;
import org.junit.jupiter.api.Test;

class ConstantDefinitionValidatorTest {
    private static final ConstantDefinition DEFAULT_CONSTANT_DEFINITION = ConstantDefinition.builder()
            .typeName(TypeName.builder()
                    .name("ConstantName")
                    .package_("package-name")
                    .build())
            .build();

    @Test
    public void testValidBoolean() {
        ConstantDefinition constantDefinition = ConstantDefinition.builder()
                .from(DEFAULT_CONSTANT_DEFINITION)
                .type(PrimitiveType.BOOLEAN)
                .value("true")
                .build();
        ConstantDefinitionValidator.validateAll(constantDefinition);
    }

    @Test
    public void testInvalidBoolean() {
        ConstantDefinition constantDefinition = ConstantDefinition.builder()
                .from(DEFAULT_CONSTANT_DEFINITION)
                .type(PrimitiveType.BOOLEAN)
                .value("nottrue")
                .build();
        assertThatThrownBy(() -> ConstantDefinitionValidator.validateAll(constantDefinition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Constant of type boolean must have value of true or false: ConstantDefinition");
    }
}

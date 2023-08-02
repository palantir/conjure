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

import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.ConstantDefinition;
import com.palantir.conjure.spec.PrimitiveType;

@com.google.errorprone.annotations.Immutable
public enum ConstantDefinitionValidator implements ConjureValidator<ConstantDefinition> {
    ConstantTypeAndValueValidator(new ConstantTypeAndValueValidator());

    public static void validateAll(ConstantDefinition definition) {
        for (ConstantDefinitionValidator validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<ConstantDefinition> validator;

    ConstantDefinitionValidator(ConjureValidator<ConstantDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ConstantDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ConstantTypeAndValueValidator implements ConjureValidator<ConstantDefinition> {

        @Override
        public void validate(ConstantDefinition definition) {

            if (definition.getType().equals(PrimitiveType.BOOLEAN)) {
                Preconditions.checkArgument(
                        definition.getValue().equalsIgnoreCase("true")
                                || definition.getValue().equalsIgnoreCase("false"),
                        "Constant of type boolean must have value of true or false: %s",
                        ConstantDefinition.class.getSimpleName());
            }
        }
    }
}

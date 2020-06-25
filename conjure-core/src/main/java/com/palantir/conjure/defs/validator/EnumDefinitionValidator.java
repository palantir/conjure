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

import com.google.common.base.Preconditions;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import java.util.HashSet;
import java.util.Set;

@com.google.errorprone.annotations.Immutable
public enum EnumDefinitionValidator implements ConjureValidator<EnumDefinition> {
    UniqueEnumValues(new UniqueEnumValuesValidator()),
    ValuesValidator(new ValuesValidator());

    public static void validateAll(EnumDefinition definition) {
        for (EnumDefinitionValidator validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<EnumDefinition> validator;

    EnumDefinitionValidator(ConjureValidator<EnumDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EnumDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueEnumValuesValidator implements ConjureValidator<EnumDefinition> {

        @Override
        public void validate(EnumDefinition definition) {
            Set<String> enumValues = new HashSet<>();
            for (EnumValueDefinition valueDef : definition.getValues()) {
                boolean unseen = enumValues.add(valueDef.getValue());
                Preconditions.checkArgument(
                        unseen,
                        "Cannot declare a %s with duplicate enum values: %s",
                        EnumTypeDefinition.class.getSimpleName(),
                        valueDef.getValue());
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class ValuesValidator implements ConjureValidator<EnumDefinition> {

        @Override
        public void validate(EnumDefinition definition) {
            definition.getValues().forEach(EnumValueDefinitionValidator::validateAll);
        }
    }
}

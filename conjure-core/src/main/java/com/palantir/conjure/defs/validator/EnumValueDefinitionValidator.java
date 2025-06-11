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
import com.palantir.conjure.EnumPattern;
import com.palantir.conjure.spec.EnumValueDefinition;

@SuppressWarnings("for-rollout:UnnecessarilyFullyQualified")
@com.google.errorprone.annotations.Immutable
public enum EnumValueDefinitionValidator implements ConjureValidator<EnumValueDefinition> {
    UnknownValueNotUsed(new UnknownValueNotUsedValidator()),
    Format(new FormatValidator());

    public static void validateAll(EnumValueDefinition definition) {
        for (ConjureValidator<EnumValueDefinition> validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<EnumValueDefinition> validator;

    EnumValueDefinitionValidator(ConjureValidator<EnumValueDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EnumValueDefinition definition) {
        validator.validate(definition);
    }

    @SuppressWarnings("for-rollout:UnnecessarilyFullyQualified")
    @com.google.errorprone.annotations.Immutable
    private static final class UnknownValueNotUsedValidator implements ConjureValidator<EnumValueDefinition> {

        @Override
        public void validate(EnumValueDefinition definition) {
            Preconditions.checkArgument(
                    !definition.getValue().equalsIgnoreCase("UNKNOWN"),
                    "UNKNOWN is a reserved enumeration value and cannot be used in an %s",
                    EnumValueDefinition.class.getSimpleName());
        }
    }

    @SuppressWarnings("for-rollout:UnnecessarilyFullyQualified")
    @com.google.errorprone.annotations.Immutable
    private static final class FormatValidator implements ConjureValidator<EnumValueDefinition> {
        private static final EnumPattern REQUIRED_FORMAT = EnumPattern.get();

        @Override
        public void validate(EnumValueDefinition definition) {
            Preconditions.checkArgument(
                    REQUIRED_FORMAT.matches(definition.getValue()),
                    "Enumeration values must match format %s: %s",
                    REQUIRED_FORMAT.pattern(),
                    definition.getValue());
        }
    }
}

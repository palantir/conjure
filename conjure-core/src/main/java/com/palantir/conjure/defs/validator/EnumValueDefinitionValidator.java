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
import com.palantir.conjure.defs.ConjureOptions;
import com.palantir.conjure.spec.EnumValueDefinition;
import java.util.regex.Pattern;

@com.google.errorprone.annotations.Immutable
public enum EnumValueDefinitionValidator implements ConjureValidator<EnumValueDefinition> {
    UnknownValueNotUsed(new UnknownValueNotUsedValidator()),
    Format(new FormatValidator());

    public static void validateAll(EnumValueDefinition definition, ConjureOptions options) {
        for (ConjureValidator<EnumValueDefinition> validator : values()) {
            validator.validate(definition, options);
        }
    }

    private final ConjureValidator<EnumValueDefinition> validator;

    EnumValueDefinitionValidator(ConjureValidator<EnumValueDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(EnumValueDefinition definition, ConjureOptions options) {
        validator.validate(definition, options);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UnknownValueNotUsedValidator implements ConjureValidator<EnumValueDefinition> {

        @Override
        public void validate(EnumValueDefinition definition, ConjureOptions _options) {
            Preconditions.checkArgument(
                    !definition.getValue().equalsIgnoreCase("UNKNOWN"),
                    "UNKNOWN is a reserved enumeration value and cannot be used in an %s",
                    EnumValueDefinition.class.getSimpleName());
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class FormatValidator implements ConjureValidator<EnumValueDefinition> {
        private static final Pattern REQUIRED_FORMAT = Pattern.compile("[A-Z][A-Z0-9]*(_[A-Z0-9]+)*");

        @Override
        public void validate(EnumValueDefinition definition, ConjureOptions _options) {
            Preconditions.checkArgument(
                    REQUIRED_FORMAT.matcher(definition.getValue()).matches(),
                    "Enumeration values must match format %s: %s",
                    REQUIRED_FORMAT,
                    definition.getValue());
        }
    }
}

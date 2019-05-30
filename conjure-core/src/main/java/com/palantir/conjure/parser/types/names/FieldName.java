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

package com.palantir.conjure.parser.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import com.palantir.conjure.CaseConverter;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the name of an {@link ObjectTypeDefinition#fields() field} of an {@link ObjectTypeDefinition}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class FieldName {

    private static final Logger log = LoggerFactory.getLogger(FieldName.class);

    @JsonValue
    public abstract String name();

    @Value.Check
    @SuppressWarnings("Slf4jLogsafeArgs")
    protected final void check() {
        CaseConverter.Case lowerCamelCase = CaseConverter.Case.LOWER_CAMEL_CASE;
        CaseConverter.Case kebabCase = CaseConverter.Case.KEBAB_CASE;
        CaseConverter.Case snakeCase = CaseConverter.Case.SNAKE_CASE;

        Preconditions.checkArgument(
                lowerCamelCase.getPattern().matcher(name()).matches()
                        || kebabCase.getPattern().matcher(name()).matches()
                        || snakeCase.getPattern().matcher(name()).matches(),
                "FieldName \"%s\" must follow one of the following patterns: %s %s %s",
                name(), lowerCamelCase, kebabCase, snakeCase);

        if (!lowerCamelCase.getPattern().matcher(name()).matches()) {
            log.warn("{} should be specified in lowerCamelCase. kebab-case and snake_case are supported for "
                    + "legacy endpoints only: {}", FieldName.class, name());
        }
    }

    /** Returns the case of this field name. */
    @Value.Lazy
    protected CaseConverter.Case nameCase() {
        for (CaseConverter.Case nameCase : CaseConverter.Case.values()) {
            if (nameCase.getPattern().matcher(name()).matches()) {
                return nameCase;
            }
        }
        throw new IllegalStateException("Could not find case for FieldName, this is a bug: " + name());
    }

    @JsonCreator
    public static FieldName of(String name) {
        return ImmutableFieldName.builder().name(name).build();
    }

    /** Converts this {@link FieldName} to a {@link FieldName} with the given case. */
    public final FieldName toCase(CaseConverter.Case targetCase) {
        return FieldName.of(nameCase().convertTo(this.name(), targetCase));
    }

    /**
     * Converts this {@link FieldName} to an upper camel case string (e.g. myVariant -> MyVariant).
     * Note that the resultant string is no longer a valid {@link FieldName}.
     */
    public final String capitalize() {
        return StringUtils.capitalize(name());
    }
}

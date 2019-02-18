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
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import java.util.Arrays;
import java.util.regex.Pattern;
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

    private static final Pattern CAMEL_CASE_PATTERN =
            Pattern.compile("^[a-z]([A-Z]{1,2}[a-z0-9]|[a-z0-9])+[A-Z]?$");
    private static final Pattern KEBAB_CASE_PATTERN =
            Pattern.compile("^[a-z]((-[a-z]){1,2}[a-z0-9]|[a-z0-9])+(-[a-z])?$");
    private static final Pattern SNAKE_CASE_PATTERN =
            Pattern.compile("^[a-z]((_[a-z]){1,2}[a-z0-9]|[a-z0-9])+(_[a-z])?$");

    public enum Case {
        LOWER_CAMEL_CASE(CAMEL_CASE_PATTERN, CaseFormat.LOWER_CAMEL),
        KEBAB_CASE(KEBAB_CASE_PATTERN, CaseFormat.LOWER_HYPHEN),
        SNAKE_CASE(SNAKE_CASE_PATTERN, CaseFormat.LOWER_UNDERSCORE);

        private final Pattern pattern;
        private final CaseFormat format;

        Case(Pattern pattern, CaseFormat format) {
            this.pattern = pattern;
            this.format = format;
        }

        @Override
        public String toString() {
            return name() + "[" + pattern + "]";
        }

        public String convertTo(FieldName fieldName, Case targetCase) {
            return format.to(targetCase.format, fieldName.name());
        }
    }

    @JsonValue
    public abstract String name();

    @Value.Check
    @SuppressWarnings("Slf4jLogsafeArgs")
    protected final void check() {
        Preconditions.checkArgument(
                Case.LOWER_CAMEL_CASE.pattern.matcher(name()).matches()
                        || Case.KEBAB_CASE.pattern.matcher(name()).matches()
                        || Case.SNAKE_CASE.pattern.matcher(name()).matches(),
                "FieldName \"%s\" must follow one of the following patterns: %s",
                name(), Arrays.toString(Case.values()));

        if (!Case.LOWER_CAMEL_CASE.pattern.matcher(name()).matches()) {
            log.warn("{} should be specified in lowerCamelCase. kebab-case and snake_case are supported for "
                    + "legacy endpoints only: {}", FieldName.class, name());
        }
    }

    /** Returns the case of this field name. */
    @Value.Lazy
    protected Case nameCase() {
        for (Case nameCase : Case.values()) {
            if (nameCase.pattern.matcher(name()).matches()) {
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
    public final FieldName toCase(Case targetCase) {
        return FieldName.of(nameCase().convertTo(this, targetCase));
    }

    /**
     * Converts this {@link FieldName} to an upper camel case string (e.g. myVariant -> MyVariant).
     * Note that the resultant string is no longer a valid {@link FieldName}.
     */
    public final String capitalize() {
        return StringUtils.capitalize(name());
    }
}

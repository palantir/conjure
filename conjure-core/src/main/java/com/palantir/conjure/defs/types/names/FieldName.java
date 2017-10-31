/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import java.util.Arrays;
import java.util.regex.Pattern;
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

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9]+([A-Z][a-z0-9]+)*$");
    private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9]+(-[a-z][a-z0-9]+)*$");
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9]+(_[a-z][a-z0-9]+)*$");

    public enum Case {
        LOWER_CAMEL_CASE(CAMEL_CASE_PATTERN) {
            @Override
            String convertTo(FieldName fieldName, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return fieldName.name();
                    case KEBAB_CASE:
                        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, fieldName.name());
                    case SNAKE_CASE:
                        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName.name());
                }
                throw new IllegalArgumentException("Unknown FieldName case, this is a bug: " + targetCase);
            }
        },
        KEBAB_CASE(KEBAB_CASE_PATTERN) {
            @Override
            String convertTo(FieldName fieldName, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, fieldName.name());
                    case KEBAB_CASE:
                        return fieldName.name();
                    case SNAKE_CASE:
                        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, fieldName.name());
                }
                throw new IllegalArgumentException("Unknown FieldName case, this is a bug: " + targetCase);
            }
        },
        SNAKE_CASE(SNAKE_CASE_PATTERN) {
            @Override
            String convertTo(FieldName fieldName, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName.name());
                    case KEBAB_CASE:
                        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, fieldName.name());
                    case SNAKE_CASE:
                        return fieldName.name();
                }
                throw new IllegalArgumentException("Unknown FieldName case, this is a bug: " + targetCase);
            }
        };

        private final Pattern pattern;

        Case(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public String toString() {
            return name() + "[" + pattern + "]";
        }

        abstract String convertTo(FieldName fieldName, Case targetCase);
    }

    @JsonValue
    public abstract String name();

    @Value.Check
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
}

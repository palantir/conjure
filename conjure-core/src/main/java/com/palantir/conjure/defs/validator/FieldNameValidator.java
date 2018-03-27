/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.palantir.conjure.spec.FieldName;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FieldNameValidator {

    private static final Logger log = LoggerFactory.getLogger(FieldName.class);
    private static final Pattern CAMEL_CASE_PATTERN =
            Pattern.compile("^[a-z]([A-Z]{1,2}[a-z0-9]|[a-z0-9])+[A-Z]?$");
    private static final Pattern KEBAB_CASE_PATTERN =
            Pattern.compile("^[a-z]((-[a-z]){1,2}[a-z0-9]|[a-z0-9])+(-[a-z])?$");
    private static final Pattern SNAKE_CASE_PATTERN =
            Pattern.compile("^[a-z]((_[a-z]){1,2}[a-z0-9]|[a-z0-9])+(_[a-z])?$");

    private FieldNameValidator() {}

    public enum Case {
        LOWER_CAMEL_CASE(CAMEL_CASE_PATTERN) {
            @Override
            String convertTo(FieldName fieldName, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return fieldName.get();
                    case KEBAB_CASE:
                        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, fieldName.get());
                    case SNAKE_CASE:
                        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName.get());
                }
                throw new IllegalArgumentException("Unknown FieldName case, this is a bug: " + targetCase);
            }
        },
        KEBAB_CASE(KEBAB_CASE_PATTERN) {
            @Override
            String convertTo(FieldName fieldName, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, fieldName.get());
                    case KEBAB_CASE:
                        return fieldName.get();
                    case SNAKE_CASE:
                        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, fieldName.get());
                }
                throw new IllegalArgumentException("Unknown FieldName case, this is a bug: " + targetCase);
            }
        },
        SNAKE_CASE(SNAKE_CASE_PATTERN) {
            @Override
            String convertTo(FieldName fieldName, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, fieldName.get());
                    case KEBAB_CASE:
                        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, fieldName.get());
                    case SNAKE_CASE:
                        return fieldName.get();
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

    /**
     * Converts this {@link FieldName} to an upper camel case string (e.g. myVariant -> MyVariant).
     * Note that the resultant string is no longer a valid {@link FieldName}.
     */
    public static String capitalize(FieldName fieldName) {
        return StringUtils.capitalize(fieldName.get());
    }

    /** Converts this {@link FieldName} to a {@link FieldName} with the given case. */
    public static FieldName toCase(FieldName fieldName, Case targetCase) {
        return FieldName.of(nameCase(fieldName).convertTo(fieldName, targetCase));
    }

    @SuppressWarnings("Slf4jLogsafeArgs")
    public static void validate(FieldName fieldName) {
        Preconditions.checkArgument(
                Case.LOWER_CAMEL_CASE.pattern.matcher(fieldName.get()).matches()
                        || Case.KEBAB_CASE.pattern.matcher(fieldName.get()).matches()
                        || Case.SNAKE_CASE.pattern.matcher(fieldName.get()).matches(),
                "FieldName \"%s\" must follow one of the following patterns: %s",
                fieldName, Arrays.toString(Case.values()));

        if (!Case.LOWER_CAMEL_CASE.pattern.matcher(fieldName.get()).matches()) {
            log.warn("{} should be specified in lowerCamelCase. kebab-case and snake_case are supported for "
                    + "legacy endpoints only: {}", FieldName.class, fieldName.get());
        }
    }

    private static Case nameCase(FieldName fieldName) {
        for (Case nameCase : Case.values()) {
            if (nameCase.pattern.matcher(fieldName.get()).matches()) {
                return nameCase;
            }
        }
        throw new IllegalStateException("Could not find case for FieldName, this is a bug: " + fieldName.get());
    }
}

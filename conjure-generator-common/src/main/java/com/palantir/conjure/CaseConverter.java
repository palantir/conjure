/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure;

import com.google.common.base.CaseFormat;
import java.util.regex.Pattern;

public final class CaseConverter {
    public static final Pattern CAMEL_CASE_PATTERN =
            Pattern.compile("^[a-z]([A-Z]{1,2}[a-z0-9]|[a-z0-9])*[A-Z]?$");
    public static final Pattern KEBAB_CASE_PATTERN =
            Pattern.compile("^[a-z]((-[a-z]){1,2}[a-z0-9]|[a-z0-9])*(-[a-z])?$");
    public static final Pattern SNAKE_CASE_PATTERN =
            Pattern.compile("^[a-z]((_[a-z]){1,2}[a-z0-9]|[a-z0-9])*(_[a-z])?$");

    private CaseConverter() {}

    public enum Case {
        LOWER_CAMEL_CASE(CAMEL_CASE_PATTERN) {
            @Override
            public String convertTo(String name, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return name;
                    case KEBAB_CASE:
                        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name);
                    case SNAKE_CASE:
                        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
                }
                throw new IllegalArgumentException("Unexpected case: " + targetCase);
            }
        },
        KEBAB_CASE(KEBAB_CASE_PATTERN) {
            @Override
            public String convertTo(String name, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name);
                    case KEBAB_CASE:
                        return name;
                    case SNAKE_CASE:
                        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, name);
                }
                throw new IllegalArgumentException("Unexpected case: " + targetCase);
            }
        },
        SNAKE_CASE(SNAKE_CASE_PATTERN) {
            @Override
            public String convertTo(String name, Case targetCase) {
                switch (targetCase) {
                    case LOWER_CAMEL_CASE:
                        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
                    case KEBAB_CASE:
                        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name);
                    case SNAKE_CASE:
                        return name;
                }
                throw new IllegalArgumentException("Unexpected case: " + targetCase);
            }
        };

        private final Pattern pattern;

        Case(Pattern pattern) {
            this.pattern = pattern;
        }

        public Pattern getPattern() {
            return pattern;
        }

        @Override
        public String toString() {
            return name() + "[" + pattern + "]";
        }

        public abstract String convertTo(String name, Case targetCase);
    }

    public static String toCase(String name, Case targetCase) {
        return nameCase(name).convertTo(name, targetCase);
    }

    private static Case nameCase(String name) {
        for (Case nameCase : Case.values()) {
            if (nameCase.pattern.matcher(name).matches()) {
                return nameCase;
            }
        }
        throw new IllegalArgumentException("Unexpected case for: " + name);
    }
}

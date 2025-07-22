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

package com.palantir.conjure;

import com.google.common.base.CaseFormat;
import com.google.errorprone.annotations.Immutable;

@Immutable
public final class CaseConverter {
    public static final CamelCasePattern CAMEL_CASE_PATTERN = CamelCasePattern.INSTANCE;
    public static final KebabCasePattern KEBAB_CASE_PATTERN = KebabCasePattern.INSTANCE;
    public static final SnakeCasePattern SNAKE_CASE_PATTERN = SnakeCasePattern.INSTANCE;

    private CaseConverter() {}

    public enum Case {
        LOWER_CAMEL_CASE(CAMEL_CASE_PATTERN) {
            @Override
            public String convertTo(String name, Case targetCase) {
                return switch (targetCase) {
                    case LOWER_CAMEL_CASE -> name;
                    case KEBAB_CASE -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, name);
                    case SNAKE_CASE -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
                };
            }
        },
        KEBAB_CASE(KEBAB_CASE_PATTERN) {
            @Override
            public String convertTo(String name, Case targetCase) {
                return switch (targetCase) {
                    case LOWER_CAMEL_CASE -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name);
                    case KEBAB_CASE -> name;
                    case SNAKE_CASE -> CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_UNDERSCORE, name);
                };
            }
        },
        SNAKE_CASE(SNAKE_CASE_PATTERN) {
            @Override
            public String convertTo(String name, Case targetCase) {
                return switch (targetCase) {
                    case LOWER_CAMEL_CASE -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name);
                    case KEBAB_CASE -> CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, name);
                    case SNAKE_CASE -> name;
                };
            }
        };

        private final SimplifiedPattern pattern;

        Case(SimplifiedPattern pattern) {
            this.pattern = pattern;
        }

        public SimplifiedPattern getPattern() {
            return pattern;
        }

        @Override
        public String toString() {
            return name() + "[" + pattern.pattern() + "]";
        }

        public abstract String convertTo(String name, Case targetCase);
    }

    public static String toCase(String name, Case targetCase) {
        return nameCase(name).convertTo(name, targetCase);
    }

    private static Case nameCase(String name) {
        for (Case nameCase : Case.values()) {
            if (nameCase.pattern.matches(name)) {
                return nameCase;
            }
        }

        throw new IllegalArgumentException("Unexpected case for: '" + name
                + "'. Expected to be in one of these case formats: [lowerCamelCase, kebab-case, snake_case]");
    }
}

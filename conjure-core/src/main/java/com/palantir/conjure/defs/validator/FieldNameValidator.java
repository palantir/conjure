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
import com.palantir.conjure.CaseConverter;
import com.palantir.conjure.spec.FieldName;
import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FieldNameValidator {

    private static final Logger log = LoggerFactory.getLogger(FieldNameValidator.class);

    private FieldNameValidator() {}

    /**
     * Converts this {@link FieldName} to an upper camel case string (e.g. myVariant -> MyVariant).
     * Note that the resultant string is no longer a valid {@link FieldName}.
     */
    public static String capitalize(FieldName fieldName) {
        return StringUtils.capitalize(fieldName.get());
    }

    /** Converts this {@link FieldName} to a {@link FieldName} with the given case. */
    public static FieldName toCase(FieldName fieldName, CaseConverter.Case targetCase) {
        return FieldName.of(nameCase(fieldName).convertTo(fieldName.get(), targetCase));
    }

    @SuppressWarnings("Slf4jLogsafeArgs")
    public static void validate(FieldName fieldName) {
        Preconditions.checkArgument(
                CaseConverter.CAMEL_CASE_PATTERN.matcher(fieldName.get()).matches()
                        || CaseConverter.KEBAB_CASE_PATTERN.matcher(fieldName.get()).matches()
                        || CaseConverter.SNAKE_CASE_PATTERN.matcher(fieldName.get()).matches(),
                "FieldName \"%s\" must follow one of the following patterns: %s",
                fieldName, Arrays.toString(CaseConverter.Case.values()));

        if (!CaseConverter.CAMEL_CASE_PATTERN.matcher(fieldName.get()).matches()) {
            log.warn("{} should be specified in lowerCamelCase. kebab-case and snake_case are supported for "
                    + "legacy endpoints only: {}", FieldName.class, fieldName.get());
        }
    }

    private static CaseConverter.Case nameCase(FieldName fieldName) {
        for (CaseConverter.Case nameCase : CaseConverter.Case.values()) {
            if (nameCase.getPattern().matcher(fieldName.get()).matches()) {
                return nameCase;
            }
        }
        throw new IllegalStateException("Could not find case for FieldName, this is a bug: " + fieldName.get());
    }
}

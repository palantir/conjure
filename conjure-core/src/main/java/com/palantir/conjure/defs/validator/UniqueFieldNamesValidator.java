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
import com.palantir.conjure.defs.ConjureOptions;
import com.palantir.conjure.spec.FieldName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Verifies that field names are unique modulo normalization to
 * {@link CaseConverter.Case#LOWER_CAMEL_CASE lower camel case}.
 */
@com.google.errorprone.annotations.Immutable
public final class UniqueFieldNamesValidator implements ConjureValidator<Set<FieldName>> {

    private final String classSimpleName;

    public UniqueFieldNamesValidator(Class<?> clazz) {
        classSimpleName = clazz.getSimpleName();
    }

    @Override
    public void validate(Set<FieldName> args, ConjureOptions _options) {
        Map<FieldName, FieldName> seenNormalizedToOriginal = new HashMap<>();
        for (FieldName argName : args) {
            FieldName normalizedName = FieldNameValidator.toCase(argName, CaseConverter.Case.LOWER_CAMEL_CASE);
            FieldName seenName = seenNormalizedToOriginal.get(normalizedName);
            Preconditions.checkArgument(
                    seenName == null,
                    "%s must not contain duplicate field names (modulo case normalization): %s vs %s",
                    classSimpleName,
                    argName.get(),
                    seenName == null ? "" : seenName.get());
            seenNormalizedToOriginal.put(normalizedName, argName);
        }
    }
}

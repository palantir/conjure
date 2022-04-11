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

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.exceptions.ConjureIllegalStateException;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.FieldName;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ErrorDefinitionValidator {

    private ErrorDefinitionValidator() {}

    private static final UniqueFieldNamesValidator UNIQUE_FIELD_NAMES_VALIDATOR =
            new UniqueFieldNamesValidator(ErrorDefinition.class);

    public static void validate(ErrorDefinition definition) {
        UNIQUE_FIELD_NAMES_VALIDATOR.validate(
                Stream.concat(definition.getSafeArgs().stream(), definition.getUnsafeArgs().stream())
                        .map(FieldDefinition::getFieldName)
                        .collect(Collectors.toSet()));
        ImmutableList<FieldName> fieldsDeclaringSafety = Stream.concat(
                        definition.getSafeArgs().stream(), definition.getUnsafeArgs().stream())
                .filter(arg -> arg.getSafety().isPresent())
                .map(FieldDefinition::getFieldName)
                .collect(ImmutableList.toImmutableList());
        if (!fieldsDeclaringSafety.isEmpty()) {
            throw new ConjureIllegalStateException("ErrorDefinition field safety is defined by the key 'safeArgs' or "
                    + "'unsafeArgs', safety cannot be declared: "
                    + fieldsDeclaringSafety + " in error " + definition.getErrorName());
        }
    }
}

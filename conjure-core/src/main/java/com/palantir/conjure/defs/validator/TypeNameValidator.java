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
import com.google.common.collect.Lists;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.TypeName;
import java.util.List;
import java.util.regex.Pattern;

public final class TypeNameValidator {

    private TypeNameValidator() {}

    private static final Pattern CUSTOM_TYPE_PATTERN = Pattern.compile("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$");

    static final List<String> PRIMITIVE_TYPES = Lists.transform(
            java.util.Arrays.asList(PrimitiveType.Value.values()), PrimitiveType.Value::name);

    public static void validate(TypeName typeName) {
        Preconditions.checkArgument(
                CUSTOM_TYPE_PATTERN.matcher(typeName.getName()).matches()
                        || PRIMITIVE_TYPES.contains(typeName.getName()),
                "TypeNames must be a primitive type %s or match pattern %s: %s",
                PRIMITIVE_TYPES, CUSTOM_TYPE_PATTERN, typeName.getName());
    }
}

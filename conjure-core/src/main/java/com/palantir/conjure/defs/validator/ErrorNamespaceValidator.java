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
import com.palantir.conjure.spec.ErrorNamespace;
import java.util.regex.Pattern;

public final class ErrorNamespaceValidator {

    private ErrorNamespaceValidator() {}

    private static final Pattern UPPER_CAMEL_CASE = Pattern.compile("(([A-Z][a-z0-9]+)+)");

    public static void validate(ErrorNamespace name) {
        Preconditions.checkArgument(
                UPPER_CAMEL_CASE.matcher(name.get()).matches(),
                "Namespace for errors must match pattern %s: %s",
                UPPER_CAMEL_CASE,
                name);
    }
}

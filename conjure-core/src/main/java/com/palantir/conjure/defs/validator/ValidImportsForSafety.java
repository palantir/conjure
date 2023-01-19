/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
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

import java.util.Map;

final class ValidImportsForSafety {

    /*
    Limiting to boxed primitives for now, but would eventually want to support more external imports.
     */
    static final Map<String, String> ALLOWED_IMPORTS = Map.ofEntries(
            Map.entry("java.lang.Long", "string"),
            Map.entry("java.lang.Integer", "integer"),
            Map.entry("java.lang.Boolean", "boolean"),
            Map.entry("java.lang.Character", "string"),
            Map.entry("java.lang.Float", "double"),
            Map.entry("java.lang.Short", "short"),
            Map.entry("java.lang.Double", "double"),
            Map.entry("java.lang.Byte", "binary"));

    private ValidImportsForSafety() {}
}

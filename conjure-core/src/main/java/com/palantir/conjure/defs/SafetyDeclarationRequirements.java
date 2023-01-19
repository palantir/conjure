/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.defs;

public enum SafetyDeclarationRequirements {
    ALLOWED,
    REQUIRED,

    EXTERNAL_IMPORTS_REQUIRED;

    public boolean required() {
        return this == REQUIRED;
    }

    public boolean external_imports_required() {
        return this == EXTERNAL_IMPORTS_REQUIRED;
    }

    public boolean allowed() {
        return this == ALLOWED;
    }

    public boolean strictMode() {
        return this == REQUIRED || this == EXTERNAL_IMPORTS_REQUIRED;
    }
}

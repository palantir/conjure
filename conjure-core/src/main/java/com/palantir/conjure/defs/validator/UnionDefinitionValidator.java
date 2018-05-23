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
import com.google.common.base.Strings;
import com.palantir.conjure.spec.UnionDefinition;

@com.google.errorprone.annotations.Immutable
public enum UnionDefinitionValidator implements ConjureValidator<UnionDefinition> {
    KEY_SYNTAX(new KeySyntaxValidator()),
    NO_TRAILING_UNDERSCORE(new NoTrailingUnderscoreValidator());

    public static void validateAll(UnionDefinition definition) {
        for (UnionDefinitionValidator validator : values()) {
            validator.validate(definition);
        }
    }

    private final ConjureValidator<UnionDefinition> validator;

    UnionDefinitionValidator(ConjureValidator<UnionDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(UnionDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoTrailingUnderscoreValidator implements ConjureValidator<UnionDefinition> {

        @Override
        public void validate(UnionDefinition definition) {
            definition.getUnion().stream().forEach(fieldDef -> {
                Preconditions.checkArgument(!fieldDef.getFieldName().get().endsWith("_"),
                        "Union member key must not end with an underscore: %s",
                        fieldDef.getFieldName().get());
            });
        }

    }

    @com.google.errorprone.annotations.Immutable
    private static final class KeySyntaxValidator implements ConjureValidator<UnionDefinition> {

        private static boolean isValidJavaIdentifier(String key) {
            if (!Character.isJavaIdentifierStart(key.charAt(0))) {
                return false;
            }
            for (char c : key.substring(1).toCharArray()) {
                if (!Character.isJavaIdentifierPart(c)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void validate(UnionDefinition definition) {
            definition.getUnion().stream().forEach(fieldDef -> {
                        Preconditions.checkArgument(!Strings.isNullOrEmpty(fieldDef.getFieldName().get()),
                                "Union member key must not be empty");
                        Preconditions.checkArgument(isValidJavaIdentifier(fieldDef.getFieldName().get()),
                                "Union member key must be a valid Java identifier: %s",
                                fieldDef.getFieldName().get());
                    }
            );
        }
    }
}

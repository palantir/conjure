/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureValidator;

@com.google.errorprone.annotations.Immutable
public enum UnionTypeDefinitionValidator implements ConjureValidator<UnionTypeDefinition> {
    KEY_SYNTAX(new KeySyntaxValidator()),
    NO_TRAILING_UNDERSCORE(new NoTrailingUnderscoreValidator());


    private final ConjureValidator<UnionTypeDefinition> validator;

    UnionTypeDefinitionValidator(ConjureValidator<UnionTypeDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(UnionTypeDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoTrailingUnderscoreValidator implements ConjureValidator<UnionTypeDefinition> {

        @Override
        public void validate(UnionTypeDefinition definition) {
            definition.union().keySet().forEach(key -> {
                Preconditions.checkArgument(!key.endsWith("_"),
                        "Union member key must not end with an underscore: %s", key);
            });
        }

    }

    @com.google.errorprone.annotations.Immutable
    private static final class KeySyntaxValidator implements ConjureValidator<UnionTypeDefinition> {

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
        public void validate(UnionTypeDefinition definition) {
            definition.union().keySet().forEach(key -> {
                        Preconditions.checkArgument(!key.isEmpty(), "Union member key must not be empty");
                        Preconditions.checkArgument(isValidJavaIdentifier(key),
                                "Union member key must be a valid Java identifier: %s", key);
                    }
            );
        }
    }
}

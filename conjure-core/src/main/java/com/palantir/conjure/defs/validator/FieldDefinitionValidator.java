/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import com.palantir.conjure.defs.visitor.TypeVisitor;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.MapType;

public final class FieldDefinitionValidator {

    private FieldDefinitionValidator() {}

    public static void validate(FieldDefinition definition) {
        checkForComplexType(definition);
    }

    private static void checkForComplexType(FieldDefinition typeDef) {
        if (typeDef.getType().accept(TypeVisitor.IS_MAP)) {
            MapType mapType = typeDef.getType().accept(TypeVisitor.MAP);
            if (!mapType.getKeyType().accept(TypeVisitor.IS_PRIMITIVE_OR_REFERENCE)) {
                throw new IllegalStateException(
                        String.format("Complex type '%s' not allowed in map key: %s.",
                                mapType.getKeyType(), typeDef));
            }
        }
    }
}


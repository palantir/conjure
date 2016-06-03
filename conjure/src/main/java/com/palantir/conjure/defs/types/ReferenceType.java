/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ReferenceType extends ConjureType {

    String type();

    static ReferenceType of(String type) {
        switch (type) {
            case "String":
                return PrimitiveTypes.String;
            case "Integer":
                return PrimitiveTypes.Integer;
            case "Double":
                return PrimitiveTypes.Double;
            default:
                return ImmutableReferenceType.builder().type(type).build();
        }
    }

}

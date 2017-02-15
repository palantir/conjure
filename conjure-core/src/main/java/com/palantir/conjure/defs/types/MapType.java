/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface MapType extends ConjureType {

    @JsonProperty("key-type")
    ConjureType keyType();

    @JsonProperty("value-type")
    ConjureType valueType();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static MapType of(ConjureType keyType, ConjureType valueType) {
        return ImmutableMapType.builder().keyType(keyType).valueType(valueType).build();
    }

}

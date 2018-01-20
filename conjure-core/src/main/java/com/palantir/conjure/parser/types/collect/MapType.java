/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.collect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
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
        return visitor.visitMap(this);
    }

    static MapType of(ConjureType keyType, ConjureType valueType) {
        return ImmutableMapType.builder().keyType(keyType).valueType(valueType).build();
    }

}

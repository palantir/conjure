/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.collect;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface SetType extends ConjureType {

    @JsonProperty("item-type")
    ConjureType itemType();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitSet(this);
    }

    static SetType of(ConjureType itemType) {
        return ImmutableSetType.builder().itemType(itemType).build();
    }

}

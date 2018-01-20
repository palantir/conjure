/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.collect;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface MapType extends ConjureType {

    ConjureType keyType();

    ConjureType valueType();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitMap(this);
    }

    static MapType of(ConjureType keyType, ConjureType valueType) {
        return ImmutableMapType.builder().keyType(keyType).valueType(valueType).build();
    }
}

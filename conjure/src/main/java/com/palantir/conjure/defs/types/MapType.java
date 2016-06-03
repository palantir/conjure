/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface MapType extends ConjureType {

    ConjureType keyType();

    ConjureType valueType();

    static MapType of(ConjureType keyType, ConjureType valueType) {
        return ImmutableMapType.builder().keyType(keyType).valueType(valueType).build();
    }

}

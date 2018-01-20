/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface FieldDefinition {

    ConjureType type();

    Optional<String> docs();

    static FieldDefinition of(ConjureType type, Optional<String> docs) {
        return ImmutableFieldDefinition.builder().type(type).docs(docs).build();
    }
}

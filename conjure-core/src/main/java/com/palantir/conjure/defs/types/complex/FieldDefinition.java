/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.Documentation;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.names.FieldName;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface FieldDefinition {

    FieldName fieldName();

    Type type();

    Optional<Documentation> docs();

    static FieldDefinition of(FieldName fieldName, Type type, Optional<Documentation> docs) {
        return ImmutableFieldDefinition.builder().fieldName(fieldName).type(type).docs(docs).build();
    }
}

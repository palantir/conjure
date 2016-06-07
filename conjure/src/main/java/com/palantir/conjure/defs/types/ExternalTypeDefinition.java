/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableExternalTypeDefinition.class)
@Value.Immutable
public interface ExternalTypeDefinition {

    Map<String, String> external();

    @Value.Default
    default PrimitiveType baseType() {
        return PrimitiveType.String;
    }

    static ExternalTypeDefinition javaType(String external) {
        return ImmutableExternalTypeDefinition.builder().putExternal("java", external).build();
    }

}

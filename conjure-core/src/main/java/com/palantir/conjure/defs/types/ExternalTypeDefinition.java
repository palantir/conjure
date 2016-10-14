/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableExternalTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ExternalTypeDefinition {

    @JsonProperty("external")
    Map<String, String> external();

    @JsonProperty("base-type")
    @Value.Default
    default PrimitiveType baseType() {
        return PrimitiveType.STRING;
    }

    static ExternalTypeDefinition javaType(String external) {
        return ImmutableExternalTypeDefinition.builder().putExternal("java", external).build();
    }

}

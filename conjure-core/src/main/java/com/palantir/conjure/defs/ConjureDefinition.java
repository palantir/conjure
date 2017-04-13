/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableConjureDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureDefinition {

    @Value.Default
    default TypesDefinition types() {
        return TypesDefinition.builder().build();
    }

    Map<TypeName, ServiceDefinition> services();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableConjureDefinition.Builder {}

}

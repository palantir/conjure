/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.services.ServiceDefinition;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.conjure.parser.types.names.TypeName;
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

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    RequestLineDefinition http();

    AuthDefinition auth();

    Map<ParameterName, ArgumentDefinition> args();

    Set<ConjureType> markers();

    Optional<ConjureType> returns();

    Optional<String> docs();

    Optional<String> deprecated();

    @Value.Check
    default void check() {
        for (EndpointDefinitionValidator validator : EndpointDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}

}

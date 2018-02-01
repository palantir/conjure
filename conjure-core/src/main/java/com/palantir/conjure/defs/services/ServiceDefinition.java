/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ServiceDefinition {

    TypeName serviceName();

    Optional<String> docs();

    Map<String, EndpointDefinition> endpoints();

    @Value.Check
    default void check() {
        for (ServiceDefinitionValidator validator : ServiceDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableServiceDefinition.Builder {}

}

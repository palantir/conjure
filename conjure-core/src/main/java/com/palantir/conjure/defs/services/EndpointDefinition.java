/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.Documentation;
import com.palantir.conjure.defs.types.Type;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    EndpointName endpointName();

    // TODO(qchen): split this into method and path?
    RequestLineDefinition http();

    // TODO(qchen): optional union type
    AuthDefinition auth();

    List<ArgumentDefinition> args();

    List<Type> markers();

    Optional<Type> returns();

    Optional<Documentation> docs();

    Optional<Documentation> deprecated();

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

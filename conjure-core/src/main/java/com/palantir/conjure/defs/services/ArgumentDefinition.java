/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ArgumentDefinition {

    ConjureType type();

    // TODO(rfink): This is duplicative with the key in Map<ParameterName, ArgumentDefinition>
    ParameterName paramId();

    ParamType paramType();

    Optional<String> docs();

    Set<ConjureType> markers();

    enum ParamType {
        /**
         * Treat as a PathParam.
         */
        PATH,
        /**
         * Treat as a QueryParam.
         */
        QUERY,
        /**
         * Treat as a HeaderParam.
         */
        HEADER,
        /**
         * Treat as the message body.
         */
        BODY
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableArgumentDefinition.Builder {}
}

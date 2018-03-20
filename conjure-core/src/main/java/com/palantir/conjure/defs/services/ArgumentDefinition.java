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
public interface ArgumentDefinition {

    ArgumentName argName();

    Type type();

    ParameterType paramType();

    Optional<Documentation> docs();

    List<Type> markers();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableArgumentDefinition.Builder {}
}

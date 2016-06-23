/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.services.ServiceDefinition;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableConjureDefinition.class)
@JsonSerialize(as = ImmutableConjureDefinition.class)
@Value.Immutable
public interface ConjureDefinition {

    TypesDefinition types();

    Map<String, ServiceDefinition> services();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableConjureDefinition.Builder {}

}

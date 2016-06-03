/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.types.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableTypesDefinition.class)
@JsonSerialize(as = ImmutableTypesDefinition.class)
@ConjureImmutablesStyle
@Value.Immutable
public interface TypesDefinition {

    String defaultPackage();

    Map<String, ExternalTypeDefinition> dependencies();

    Map<String, ObjectTypeDefinition> definitions();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}

}

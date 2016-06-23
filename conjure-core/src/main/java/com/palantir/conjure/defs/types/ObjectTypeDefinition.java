/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableObjectTypeDefinition.class)
@Value.Immutable
public interface ObjectTypeDefinition {

    @JsonProperty("package")
    Optional<String> packageName();

    Map<String, FieldDefinition> fields();

    Optional<String> docs();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectTypeDefinition.Builder {}

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableAliasTypeDefinition.class)
@JsonSerialize(as = ImmutableAliasTypeDefinition.class)
@Value.Immutable
public interface AliasTypeDefinition extends BaseObjectTypeDefinition {

    PrimitiveType alias();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectsDefinition.class)
@JsonSerialize(as = ImmutableObjectsDefinition.class)
@ConjureImmutablesStyle
@Value.Immutable
public interface ObjectsDefinition {

    String defaultPackage();

    Map<String, BaseObjectTypeDefinition> objects();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectsDefinition.Builder {}

}

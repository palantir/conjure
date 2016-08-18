/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableObjectTypeDefinition.class)
@Value.Immutable
public interface ObjectTypeDefinition extends BaseObjectTypeDefinition {

    Map<String, FieldDefinition> fields();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectTypeDefinition.Builder {}

}

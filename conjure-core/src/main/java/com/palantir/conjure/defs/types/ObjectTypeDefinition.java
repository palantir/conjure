/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectTypeDefinition extends BaseObjectTypeDefinition {

    Map<FieldName, FieldDefinition> fields();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectTypeDefinition.Builder {}

}

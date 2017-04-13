/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import java.io.IOException;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableUnionTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface UnionTypeDefinition extends BaseObjectTypeDefinition {

    Map<String, UnionMemberTypeDefinition> union();

    @Value.Check
    default void check() {
        for (UnionTypeDefinitionValidator validator : UnionTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static UnionTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, UnionTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableUnionTypeDefinition.Builder {}

}

/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.reference;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import java.io.IOException;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableAliasTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface AliasTypeDefinition extends BaseObjectTypeDefinition {

    ConjureType alias();

    @Override
    default <T> T visit(TypeDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static Builder builder() {
        return new Builder();
    }

    static AliasTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, AliasTypeDefinition.class);
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}

}

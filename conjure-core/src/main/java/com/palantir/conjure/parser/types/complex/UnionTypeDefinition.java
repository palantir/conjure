/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import com.palantir.conjure.parser.types.names.FieldName;
import java.io.IOException;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableUnionTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface UnionTypeDefinition extends BaseObjectTypeDefinition {

    Map<FieldName, FieldDefinition> union();

    @Override
    default <T> T visit(TypeDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static UnionTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, UnionTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableUnionTypeDefinition.Builder {}

}

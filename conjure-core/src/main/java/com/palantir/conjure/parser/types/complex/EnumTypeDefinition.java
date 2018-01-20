/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.ObjectTypeDefVisitor;
import java.io.IOException;
import java.util.List;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEnumTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EnumTypeDefinition extends BaseObjectTypeDefinition {

    List<EnumValueDefinition> values();

    @Override
    default <T> T visit(ObjectTypeDefVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static EnumTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, EnumTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumTypeDefinition.Builder {}
}

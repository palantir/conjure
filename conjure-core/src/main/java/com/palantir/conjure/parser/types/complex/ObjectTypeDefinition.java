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
import com.palantir.conjure.parser.types.names.FieldName;
import java.io.IOException;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectTypeDefinition extends BaseObjectTypeDefinition {

    Map<FieldName, FieldDefinition> fields();

    @Override
    default <T> T visit(ObjectTypeDefVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static ObjectTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, ImmutableObjectTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectTypeDefinition.Builder {}

}

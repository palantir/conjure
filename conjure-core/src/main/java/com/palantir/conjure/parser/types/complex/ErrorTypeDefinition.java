/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.complex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import com.palantir.conjure.parser.types.names.ErrorCode;
import com.palantir.conjure.parser.types.names.ErrorNamespace;
import com.palantir.conjure.parser.types.names.FieldName;
import java.io.IOException;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableErrorTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ErrorTypeDefinition extends BaseObjectTypeDefinition {

    ErrorNamespace namespace();

    ErrorCode code();

    @JsonProperty("safe-args")
    Map<FieldName, FieldDefinition> safeArgs();

    @JsonProperty("unsafe-args")
    Map<FieldName, FieldDefinition> unsafeArgs();

    @Override
    default <T> T visit(TypeDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static ErrorTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, ImmutableErrorTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableErrorTypeDefinition.Builder {}

}

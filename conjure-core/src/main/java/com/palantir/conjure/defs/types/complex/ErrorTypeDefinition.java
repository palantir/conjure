/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.ErrorCode;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import com.palantir.conjure.defs.types.names.FieldName;
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

    @Value.Check
    default void check() {
        for (ErrorTypeDefinitionValidator validator : ErrorTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static ErrorTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, ImmutableErrorTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableErrorTypeDefinition.Builder {}

}

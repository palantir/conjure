/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition.BaseObjectTypeDefinitionDeserializer;
import java.io.IOException;
import java.util.Optional;

@JsonDeserialize(using = BaseObjectTypeDefinitionDeserializer.class)
public interface BaseObjectTypeDefinition {

    @JsonProperty("package")
    Optional<String> packageName();

    Optional<String> docs();

    class BaseObjectTypeDefinitionDeserializer extends JsonDeserializer<BaseObjectTypeDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public BaseObjectTypeDefinition deserialize(JsonParser parser, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            TreeNode tree = parser.readValueAsTree();
            if (tree.get("fields") != null) {
                return ImmutableObjectTypeDefinition.fromJson(
                        parser.getCodec().treeToValue(tree, ImmutableObjectTypeDefinition.Json.class));
            } else if (tree.get("values") != null) {
                return ImmutableEnumTypeDefinition.fromJson(
                        parser.getCodec().treeToValue(tree, ImmutableEnumTypeDefinition.Json.class));
            } else {
                throw new IllegalArgumentException(
                        "Unrecognized definition, objects must have either fields or values defined.");
            }
        }
    }

}

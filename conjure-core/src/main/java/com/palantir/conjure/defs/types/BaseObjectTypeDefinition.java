/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition.BaseObjectTypeDefinitionDeserializer;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinition;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.complex.UnionTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.reference.AliasTypeDefinition;
import java.io.IOException;
import java.util.Optional;

@JsonDeserialize(using = BaseObjectTypeDefinitionDeserializer.class)
public interface BaseObjectTypeDefinition {

    @JsonProperty("package")
    Optional<ConjurePackage> conjurePackage();

    Optional<String> docs();

    class BaseObjectTypeDefinitionDeserializer extends JsonDeserializer<BaseObjectTypeDefinition> {
        @Override
        public BaseObjectTypeDefinition deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            TreeNode tree = parser.readValueAsTree();
            if (tree.get("fields") != null) {
                return ObjectTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("values") != null) {
                return EnumTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("alias") != null) {
                return AliasTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("union") != null) {
                return UnionTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("namespace") != null) {
                return ErrorTypeDefinition.fromJson(parser, tree);
            } else {
                throw new IllegalArgumentException(
                        "Unrecognized definition, objects must have either fields, values or an alias defined.");
            }
        }
    }
}

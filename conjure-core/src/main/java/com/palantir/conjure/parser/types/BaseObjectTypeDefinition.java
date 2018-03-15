/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.parser.ConjureMetrics;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition.BaseObjectTypeDefinitionDeserializer;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import java.io.IOException;
import java.util.Optional;

@JsonDeserialize(using = BaseObjectTypeDefinitionDeserializer.class)
public interface BaseObjectTypeDefinition {

    @JsonProperty("package")
    Optional<ConjurePackage> conjurePackage();

    Optional<String> docs();

    <T> T visit(TypeDefinitionVisitor<T> visitor);

    class BaseObjectTypeDefinitionDeserializer extends JsonDeserializer<BaseObjectTypeDefinition> {
        @Override
        public BaseObjectTypeDefinition deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
            TreeNode tree = parser.readValueAsTree();
            if (tree.get("fields") != null) {
                ObjectTypeDefinition objectDef = ObjectTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(ObjectTypeDefinition.class);
                ConjureMetrics.histogram(objectDef.fields().size(), ObjectTypeDefinition.class, "fields");
                return objectDef;
            } else if (tree.get("values") != null) {
                EnumTypeDefinition enumDef = EnumTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(EnumTypeDefinition.class);
                ConjureMetrics.histogram(enumDef.values().size(), EnumTypeDefinition.class, "values");
                return enumDef;
            } else if (tree.get("alias") != null) {
                AliasTypeDefinition aliasDef = AliasTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(AliasTypeDefinition.class);
                ConjureMetrics.incrementCounter(AliasTypeDefinition.class,
                        "inner",
                        aliasDef.alias().getClass().getSimpleName());
                return aliasDef;
            } else if (tree.get("union") != null) {
                UnionTypeDefinition unionDef = UnionTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(UnionTypeDefinition.class);
                ConjureMetrics.histogram(unionDef.union().size(), UnionTypeDefinition.class, "variants");
                return unionDef;
            } else if (tree.get("namespace") != null) {
                return ErrorTypeDefinition.fromJson(parser, tree);
            } else {
                throw new IllegalArgumentException(
                        "Unrecognized definition, types must have either fields, values or an alias defined.");
            }
        }
    }
}

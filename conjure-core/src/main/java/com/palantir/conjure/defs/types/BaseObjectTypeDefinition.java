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
import com.palantir.conjure.defs.ConjureMetrics;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition.BaseObjectTypeDefinitionDeserializer;
import com.palantir.conjure.defs.types.complex.EnumTypeDefinition;
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
                ObjectTypeDefinition object = ObjectTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(ObjectTypeDefinition.class);
                ConjureMetrics.histogram(object.fields().size(), ObjectTypeDefinition.class, "fields");
                return object;
            } else if (tree.get("values") != null) {
                EnumTypeDefinition enumTypeDefinition = EnumTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(EnumTypeDefinition.class);
                ConjureMetrics.histogram(enumTypeDefinition.values().size(), EnumTypeDefinition.class, "values");
                return enumTypeDefinition;
            } else if (tree.get("alias") != null) {
                ConjureMetrics.incrementCounter(AliasTypeDefinition.class);
                AliasTypeDefinition aliasTypeDefinition = AliasTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(AliasTypeDefinition.class,
                        "inner",
                        aliasTypeDefinition.alias().getClass().getSimpleName());
                return aliasTypeDefinition;
            } else if (tree.get("union") != null) {
                UnionTypeDefinition union = UnionTypeDefinition.fromJson(parser, tree);
                ConjureMetrics.incrementCounter(UnionTypeDefinition.class);
                ConjureMetrics.histogram(union.union().size(), UnionTypeDefinition.class, "variants");
                return union;
            } else {
                throw new IllegalArgumentException(
                        "Unrecognized definition, objects must have either fields, values or an alias defined.");
            }
        }
    }
}

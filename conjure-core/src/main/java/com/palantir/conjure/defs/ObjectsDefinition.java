/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.ReferenceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectsDefinition.class)
@JsonSerialize(as = ImmutableObjectsDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectsDefinition {

    @JsonProperty("default-package")
    Optional<String> defaultPackage();

    @JsonProperty("objects")
    Map<String, BaseObjectTypeDefinition> objects();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectsDefinition.Builder {}

    @Value.Check
    default void check() {
        // create mapping from object type name -> set<string> of names of reference types that are fields of that type
        Multimap<String, String> typeToRefFields = HashMultimap.create();
        for (Map.Entry<String, BaseObjectTypeDefinition> currEntry : objects().entrySet()) {
            if (currEntry.getValue() instanceof ObjectTypeDefinition) {
                ObjectTypeDefinition objectDef = (ObjectTypeDefinition) currEntry.getValue();
                for (Map.Entry<String, FieldDefinition> currField : objectDef.fields().entrySet()) {
                    if (currField.getValue().type() instanceof ReferenceType) {
                        typeToRefFields.put(currEntry.getKey(), ((ReferenceType) currField.getValue().type()).type());
                    }
                }
            }
        }

        for (String currType : typeToRefFields.keySet()) {
            verifyTypeHasNoRecursiveDefinitions(currType, typeToRefFields, new ArrayList<>());
        }
    }

    static void verifyTypeHasNoRecursiveDefinitions(String typeName, Multimap<String, String> typeMap,
            List<String> path) {
        if (path.contains(typeName)) {
            path.add(typeName);
            throw new RuntimeException("recursive: " + Joiner.on(" -> ").join(path));
        }

        path.add(typeName);
        for (String currField : typeMap.get(typeName)) {
            verifyTypeHasNoRecursiveDefinitions(currField, typeMap, path);
        }
        path.remove(path.size() - 1);
    }

}

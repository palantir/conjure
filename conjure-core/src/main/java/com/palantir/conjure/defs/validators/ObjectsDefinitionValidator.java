/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validators;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ObjectsDefinition;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.FieldDefinition;
import com.palantir.conjure.defs.types.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.ReferenceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum ObjectsDefinitionValidator implements ConjureValidator<ObjectsDefinition> {
    NO_RECURSIVE_TYPES(new NoRecursiveTypesValidator());

    private final ConjureValidator<ObjectsDefinition> validator;

    ObjectsDefinitionValidator(ConjureValidator<ObjectsDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ObjectsDefinition definition) {
        this.validator.validate(definition);
    }

    private static final class NoRecursiveTypesValidator implements ConjureValidator<ObjectsDefinition> {
        @Override
        public void validate(ObjectsDefinition definition) {
            // create mapping from object type name -> names of reference types that are fields of that type
            Multimap<String, String> typeToRefFields = HashMultimap.create();
            for (Map.Entry<String, BaseObjectTypeDefinition> currEntry : definition.objects().entrySet()) {
                if (currEntry.getValue() instanceof ObjectTypeDefinition) {
                    ObjectTypeDefinition objectDef = (ObjectTypeDefinition) currEntry.getValue();
                    for (Map.Entry<String, FieldDefinition> currField : objectDef.fields().entrySet()) {
                        if (currField.getValue().type() instanceof ReferenceType) {
                            typeToRefFields.put(
                                    currEntry.getKey(),
                                    ((ReferenceType) currField.getValue().type()).type()
                            );
                        }
                    }
                }
            }

            for (String currType : typeToRefFields.keySet()) {
                verifyTypeHasNoRecursiveDefinitions(currType, typeToRefFields, new ArrayList<>());
            }
        }

        private void verifyTypeHasNoRecursiveDefinitions(String typeName, Multimap<String, String> typeMap,
                List<String> path) {
            if (path.contains(typeName)) {
                path.add(typeName);
                throw new IllegalStateException("Illegal recursive data type: " + Joiner.on(" -> ").join(path));
            }

            path.add(typeName);
            for (String currField : typeMap.get(typeName)) {
                verifyTypeHasNoRecursiveDefinitions(currField, typeMap, path);
            }
            path.remove(path.size() - 1);
        }
    }

}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.AliasTypeDefinition;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum ObjectsDefinitionValidator implements ConjureValidator<ObjectsDefinition> {
    NO_RECURSIVE_TYPES(new NoRecursiveTypesValidator()),
    PACKAGE_DEFINED(new PackageDefinedValidator());

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
            Multimap<TypeName, TypeName> typeToRefFields = HashMultimap.create();
            for (Map.Entry<TypeName, BaseObjectTypeDefinition> currEntry : definition.objects().entrySet()) {
                if (currEntry.getValue() instanceof ObjectTypeDefinition) {
                    ObjectTypeDefinition objectDef = (ObjectTypeDefinition) currEntry.getValue();
                    for (Map.Entry<FieldName, FieldDefinition> currField : objectDef.fields().entrySet()) {
                        if (currField.getValue().type() instanceof ReferenceType) {
                            typeToRefFields.put(
                                    currEntry.getKey(),
                                    ((ReferenceType) currField.getValue().type()).type()
                            );
                        }
                    }
                } else if (currEntry.getValue() instanceof AliasTypeDefinition) {
                    AliasTypeDefinition aliasDef = (AliasTypeDefinition) currEntry.getValue();
                    if (aliasDef.alias() instanceof ReferenceType) {
                        typeToRefFields.put(
                                currEntry.getKey(),
                                ((ReferenceType) aliasDef.alias()).type()
                        );
                    }
                }
            }

            for (TypeName currType : typeToRefFields.keySet()) {
                verifyTypeHasNoRecursiveDefinitions(currType, typeToRefFields, new ArrayList<>());
            }
        }

        private void verifyTypeHasNoRecursiveDefinitions(
                TypeName typeName, Multimap<TypeName, TypeName> typeMap, List<TypeName> path) {
            if (path.contains(typeName)) {
                path.add(typeName);
                throw new IllegalStateException("Illegal recursive data type: "
                        + Joiner.on(" -> ").join(Lists.transform(path, p -> p.name())));
            }

            path.add(typeName);
            for (TypeName currField : typeMap.get(typeName)) {
                verifyTypeHasNoRecursiveDefinitions(currField, typeMap, path);
            }
            path.remove(path.size() - 1);
        }
    }

    private static final class PackageDefinedValidator implements ConjureValidator<ObjectsDefinition> {
        @Override
        public void validate(ObjectsDefinition definition) {
            if (definition.defaultConjurePackage().isPresent()
                    && definition.defaultConjurePackage().get().name().length() > 0) {
                // default package is defined -- nothing to do
                return;
            }

            definition.objects().entrySet().stream().forEach(entry -> {
                if (!entry.getValue().conjurePackage().isPresent()) {
                    throw new IllegalStateException("No package is defined for object " + entry.getKey().name());
                }
            });
        }
    }
}

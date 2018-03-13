/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.google.common.base.Joiner;
import com.google.common.base.Verify;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.services.IsPrimitiveOrReferenceType;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.AliasTypeDefinition;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@com.google.errorprone.annotations.Immutable
public enum ObjectsDefinitionValidator implements ConjureValidator<ObjectsDefinition> {
    NO_RECURSIVE_TYPES(new NoRecursiveTypesValidator()),
    NO_COMPLEX_KEYS(new NoComplexKeysValidator()),
    UNIQUE_NAMES(new UniqueNamesValidator());

    private final ConjureValidator<ObjectsDefinition> validator;

    ObjectsDefinitionValidator(ConjureValidator<ObjectsDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ObjectsDefinition definition) {
        this.validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoRecursiveTypesValidator implements ConjureValidator<ObjectsDefinition> {
        @Override
        public void validate(ObjectsDefinition definition) {
            // create mapping from object type name -> names of reference types that are fields of that type
            Multimap<TypeName, TypeName> typeToRefFields = HashMultimap.create();
            for (BaseObjectTypeDefinition typeDef : definition.types()) {
                if (typeDef instanceof ObjectTypeDefinition) {
                    ObjectTypeDefinition objectDef = (ObjectTypeDefinition) typeDef;
                    for (FieldDefinition currField : objectDef.fields()) {
                        if (currField.type() instanceof ReferenceType) {
                            typeToRefFields.put(
                                    typeDef.typeName(),
                                    ((ReferenceType) currField.type()).type()
                            );
                        }
                    }
                } else if (typeDef instanceof AliasTypeDefinition) {
                    AliasTypeDefinition aliasDef = (AliasTypeDefinition) typeDef;
                    if (aliasDef.alias() instanceof ReferenceType) {
                        typeToRefFields.put(
                                typeDef.typeName(),
                                ((ReferenceType) aliasDef.alias()).type()
                        );
                    }
                }
            }

            for (TypeName name : typeToRefFields.keySet()) {
                verifyTypeHasNoRecursiveDefinitions(name, typeToRefFields, new ArrayList<>());
            }
        }

        private void verifyTypeHasNoRecursiveDefinitions(
                TypeName typeName, Multimap<TypeName, TypeName> typeMap, List<TypeName> path) {
            if (path.contains(typeName)) {
                path.add(typeName);
                throw new IllegalStateException("Illegal recursive data type: "
                        + Joiner.on(" -> ").join(Lists.transform(path, TypeName::name)));
            }

            path.add(typeName);
            for (TypeName currField : typeMap.get(typeName)) {
                verifyTypeHasNoRecursiveDefinitions(currField, typeMap, path);
            }
            path.remove(path.size() - 1);
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexKeysValidator implements ConjureValidator<ObjectsDefinition> {
        @Override
        public void validate(ObjectsDefinition definition) {
            definition.types().stream().forEach(typeDef -> {
                if (typeDef instanceof ObjectTypeDefinition) {
                    ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition) typeDef;
                    objectTypeDefinition.fields().stream().forEach(fieldDefinition -> {
                        if (fieldDefinition.type() instanceof MapType) {
                            MapType mapType = (MapType) fieldDefinition.type();
                            if (!mapType.keyType().visit(IsPrimitiveOrReferenceType.INSTANCE)) {
                                throw new IllegalStateException(
                                        String.format("Complex type '%s' not allowed in map key: %s.",
                                                mapType.keyType(), fieldDefinition.fieldName()));
                            }
                        }
                    });
                }
            });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueNamesValidator implements ConjureValidator<ObjectsDefinition> {
        @Override
        public void validate(ObjectsDefinition definition) {
            Set<String> seenNames = new HashSet<>();
            definition.types().forEach(typeDef -> verifyNameIsUnique(seenNames, typeDef.typeName().name()));
            definition.errors().forEach(errorDef -> verifyNameIsUnique(seenNames, errorDef.typeName().name()));
        }

        private static void verifyNameIsUnique(Set<String> seenNames, String name) {
            boolean isNewName = seenNames.add(name);
            Verify.verify(isNewName,
                    "Type and error names must be unique across locally defined and imported types: %s", name);
        }
    }
}

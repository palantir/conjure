/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static java.util.stream.Collectors.toList;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.palantir.conjure.defs.services.IsPrimitiveOrReferenceType;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.collect.MapType;
import com.palantir.conjure.defs.types.complex.AliasTypeDefinition;
import com.palantir.conjure.defs.types.complex.FieldDefinition;
import com.palantir.conjure.defs.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ReferenceType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@com.google.errorprone.annotations.Immutable
public enum ConjureDefinitionValidator implements ConjureValidator<ConjureDefinition> {
    UNIQUE_SERVICE_NAMES(new UniqueServiceNamesValidator()),
    ILLEGAL_SUFFIXES(new IllegalSuffixesValidator()),

    // can no longer validate at the types level
    NO_RECURSIVE_TYPES(new NoRecursiveTypesValidator()),
    NO_COMPLEX_KEYS(new NoComplexKeysValidator()),
    UNIQUE_NAMES(new UniqueNamesValidator());

    private final ConjureValidator<ConjureDefinition> validator;

    ConjureDefinitionValidator(ConjureValidator<ConjureDefinition> validator) {
        this.validator = validator;
    }

    @Override
    public void validate(ConjureDefinition definition) {
        validator.validate(definition);
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueServiceNamesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            Set<String> seenNames = new HashSet<>();
            definition.services().forEach(service -> {
                boolean isNewName = seenNames.add(service.serviceName().name());
                Preconditions.checkState(isNewName,
                        "Service names must be unique: %s", service.serviceName().name());
            });
        }
    }

    /**
     * This ensures that ExperimentalFeatures.DisambiguateRetrofitServices won't cause collisions.
     */
    @com.google.errorprone.annotations.Immutable
    private static final class IllegalSuffixesValidator implements ConjureValidator<ConjureDefinition> {
        private static final String RETROFIT_SUFFIX = "Retrofit";

        @Override
        public void validate(ConjureDefinition definition) {
            List<String> violations = definition.services().stream()
                    .map(def -> def.serviceName().name())
                    .filter(name -> name.endsWith(RETROFIT_SUFFIX))
                    .collect(toList());

            Preconditions.checkState(violations.isEmpty(),
                    "Service names must not end in %s: %s", RETROFIT_SUFFIX, violations);
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueNamesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            Set<String> seenNames = new HashSet<>();
            definition.types().forEach(typeDef -> verifyNameIsUnique(seenNames, typeDef.typeName().name()));
            definition.errors().forEach(errorDef -> verifyNameIsUnique(seenNames, errorDef.typeName().name()));
            definition.services().forEach(serviceDef -> verifyNameIsUnique(seenNames, serviceDef.serviceName().name()));
        }

        private static void verifyNameIsUnique(Set<String> seenNames, String name) {
            boolean isNewName = seenNames.add(name);
            Verify.verify(isNewName,
                    "Type, error, and service names must be unique across locally defined and imported "
                            + "types/errors: %s\n%s",
                    seenNames, name);
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoComplexKeysValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            definition.types().stream().forEach(NoComplexKeysValidator::validateTypeDef);
            definition.errors().stream().forEach(NoComplexKeysValidator::validateTypeDef);
        }

        private static void validateTypeDef(TypeDefinition typeDef) {
            if (typeDef instanceof ObjectTypeDefinition) {
                ObjectTypeDefinition objectTypeDefinition = (ObjectTypeDefinition) typeDef;
                objectTypeDefinition.fields().stream().forEach(fieldDefinition -> {
                    if (fieldDefinition.type() instanceof MapType) {
                        MapType mapType = (MapType) fieldDefinition.type();
                        if (!mapType.keyType().visit(IsPrimitiveOrReferenceType.INSTANCE)) {
                            throw new IllegalStateException(
                                    String.format("Complex type '%s' not allowed in map key: %s.",
                                            mapType.keyType(), ((ObjectTypeDefinition) typeDef).fields()));
                        }
                    }
                });
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoRecursiveTypesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            // create mapping from object type name -> names of reference types that are fields of that type
            Multimap<TypeName, TypeName> typeToRefFields = HashMultimap.create();
            definition.errors().stream().forEach(error -> getReferenceType(error)
                    .ifPresent(entry -> typeToRefFields.put(error.typeName(), entry)));

            definition.types().stream().forEach(type -> getReferenceType(type)
                    .ifPresent(entry -> typeToRefFields.put(type.typeName(), entry)));

            for (TypeName name : typeToRefFields.keySet()) {
                verifyTypeHasNoRecursiveDefinitions(name, typeToRefFields, new ArrayList<>());
            }
        }

        private static Optional<TypeName> getReferenceType(TypeDefinition typeDef) {
            if (typeDef instanceof ObjectTypeDefinition) {
                ObjectTypeDefinition objectDef = (ObjectTypeDefinition) typeDef;
                for (FieldDefinition currField : objectDef.fields()) {
                    if (currField.type() instanceof ReferenceType) {
                        return Optional.of(((ReferenceType) currField.type()).type());
                    }
                }
            } else if (typeDef instanceof AliasTypeDefinition) {
                AliasTypeDefinition aliasDef = (AliasTypeDefinition) typeDef;
                if (aliasDef.alias() instanceof ReferenceType) {
                    return Optional.of(((ReferenceType) aliasDef.alias()).type());
                }
            }
            return Optional.empty();
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
}

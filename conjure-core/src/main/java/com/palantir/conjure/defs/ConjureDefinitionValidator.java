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
import com.google.common.collect.Streams;
import com.palantir.conjure.defs.types.TypeDefinitionVisitor;
import com.palantir.conjure.defs.types.TypeVisitor;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
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

    public static void validateAll(ConjureDefinition definition) {
        for (ConjureDefinitionValidator validator : values()) {
            validator.validate(definition);
        }
    }

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
            definition.getServices().forEach(service -> {
                boolean isNewName = seenNames.add(service.getServiceName().getName());
                Preconditions.checkState(isNewName,
                        "Service names must be unique: %s", service.getServiceName().getName());
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
            List<String> violations = definition.getServices().stream()
                    .map(def -> def.getServiceName().getName())
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
            definition.getTypes().forEach(typeDef ->
                    verifyNameIsUnique(seenNames, typeDef.accept(TypeDefinitionVisitor.TYPE_NAME).getName()));
            definition.getErrors().forEach(errorDef ->
                    verifyNameIsUnique(seenNames, errorDef.getErrorName().getName()));
            definition.getServices().forEach(serviceDef ->
                    verifyNameIsUnique(seenNames, serviceDef.getServiceName().getName()));
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
            definition.getTypes().stream().forEach(NoComplexKeysValidator::validateTypeDef);
            definition.getErrors().stream().forEach(NoComplexKeysValidator::validateErrorDef);
        }

        private static void validateTypeDef(TypeDefinition typeDef) {
            if (typeDef.accept(TypeDefinitionVisitor.IS_OBJECT)) {
                ObjectDefinition objectDefinition = typeDef.accept(TypeDefinitionVisitor.OBJECT);
                objectDefinition.getFields().stream().forEach(NoComplexKeysValidator::checkForComplexType);
            }
        }

        private static void validateErrorDef(ErrorDefinition errorDef) {
            Streams.concat(errorDef.getSafeArgs().stream(), errorDef.getUnsafeArgs().stream())
                    .forEach(NoComplexKeysValidator::checkForComplexType);
        }

        private static void checkForComplexType(FieldDefinition typeDef) {
            if (typeDef.getType().accept(TypeVisitor.IS_MAP)) {
                MapType mapType = typeDef.getType().accept(TypeVisitor.MAP);
                if (!mapType.getKeyType().accept(TypeVisitor.IS_PRIMITIVE_OR_REFERENCE)) {
                    throw new IllegalStateException(
                            String.format("Complex type '%s' not allowed in map key: %s.",
                                    mapType.getKeyType(), typeDef));
                }
            }
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoRecursiveTypesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            // create mapping from object type name -> names of reference types that are fields of that type
            Multimap<TypeName, TypeName> typeToRefFields = HashMultimap.create();

            definition.getTypes().stream().forEach(type ->
                    getReferenceType(type)
                    .ifPresent(entry -> typeToRefFields.put(
                            type.accept(TypeDefinitionVisitor.TYPE_NAME), entry)));

            for (TypeName name : typeToRefFields.keySet()) {
                verifyTypeHasNoRecursiveDefinitions(name, typeToRefFields, new ArrayList<>());
            }
        }

        private static Optional<TypeName> getReferenceType(TypeDefinition typeDef) {
            if (typeDef.accept(TypeDefinitionVisitor.IS_OBJECT)) {
                ObjectDefinition objectDef = typeDef.accept(TypeDefinitionVisitor.OBJECT);
                for (FieldDefinition currField : objectDef.getFields()) {
                    Optional<TypeName> referenceType = resolveReferenceType(currField.getType());
                    if (referenceType.isPresent()) {
                        return referenceType;
                    }
                }
            } else if (typeDef.accept(TypeDefinitionVisitor.IS_ALIAS)) {
                AliasDefinition aliasDef = typeDef.accept(TypeDefinitionVisitor.ALIAS);
                return resolveReferenceType(aliasDef.getAlias());
            }
            return Optional.empty();
        }

        private static Optional<TypeName> resolveReferenceType(Type type) {
            if (type.accept(TypeVisitor.IS_REFERENCE)) {
                return Optional.of(type.accept(TypeVisitor.REFERENCE));
            } else if (type.accept(TypeVisitor.IS_PRIMITIVE)) {
                return Optional.of(
                        TypeName.of(type.accept(TypeVisitor.PRIMITIVE).get().name(), ""));
            }
            return Optional.empty();
        }

        private void verifyTypeHasNoRecursiveDefinitions(
                TypeName typeName, Multimap<TypeName, TypeName> typeMap, List<TypeName> path) {
            if (path.contains(typeName)) {
                path.add(typeName);
                throw new IllegalStateException("Illegal recursive data type: "
                        + Joiner.on(" -> ").join(Lists.transform(path, TypeName::getName)));
            }

            path.add(typeName);
            for (TypeName currField : typeMap.get(typeName)) {
                verifyTypeHasNoRecursiveDefinitions(currField, typeMap, path);
            }
            path.remove(path.size() - 1);
        }
    }
}

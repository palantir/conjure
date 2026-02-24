/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.defs.validator;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.SafetyDeclarationRequirements;
import com.palantir.conjure.exceptions.ConjureIllegalStateException;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.ErrorDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.ServiceDefinition;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import com.palantir.conjure.visitor.TypeVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@com.google.errorprone.annotations.Immutable
public enum ConjureDefinitionValidator implements ConjureValidator<ConjureDefinition> {
    UNIQUE_SERVICE_NAMES(new UniqueServiceNamesValidator()),
    ILLEGAL_VERSION(new IllegalVersionValidator()),
    NO_RECURSIVE_TYPES(new NoRecursiveTypesValidator()),
    UNIQUE_NAMES(new UniqueNamesValidator()),
    NO_NESTED_OPTIONAL(new NoNestedOptionalValidator()),
    ILLEGAL_MAP_KEYS(new IllegalMapKeyValidator()),
    UNIQUE_ERROR_NAMES(UniqueErrorNameValidator.INSTANCE);

    public static void validateAll(ConjureDefinition definition, SafetyDeclarationRequirements requirements) {
        for (ConjureValidator<ConjureDefinition> validator : values()) {
            validator.validate(definition);
        }
        new LogSafetyConjureDefinitionValidator(requirements).validate(definition);
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
                Preconditions.checkState(
                        isNewName,
                        "Service names must be unique: %s",
                        service.getServiceName().getName());
            });
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class IllegalVersionValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            Preconditions.checkState(
                    definition.getVersion() == Conjure.SUPPORTED_IR_VERSION,
                    "Definition version must be %s, but version %s is provided instead.",
                    Conjure.SUPPORTED_IR_VERSION,
                    definition.getVersion());
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class UniqueNamesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            Set<TypeName> seenNames = new HashSet<>();
            definition
                    .getTypes()
                    .forEach(typeDef -> verifyNameIsUnique(seenNames, typeDef.accept(TypeDefinitionVisitor.TYPE_NAME)));
            definition.getErrors().forEach(errorDef -> verifyNameIsUnique(seenNames, errorDef.getErrorName()));
            definition.getServices().forEach(serviceDef -> verifyNameIsUnique(seenNames, serviceDef.getServiceName()));
        }

        private static void verifyNameIsUnique(Set<TypeName> seenNames, TypeName name) {
            if (!seenNames.add(name)) {
                throw new ConjureIllegalStateException(String.format(
                        "Type, error, and service names must be unique across locally defined and imported "
                                + "types/errors:\nFound duplicate name: %s\nKnown names:\n - %s",
                        typeNameToString(name),
                        seenNames.stream()
                                .map(UniqueNamesValidator::typeNameToString)
                                .collect(Collectors.joining("\n - "))));
            }
        }

        private static String typeNameToString(TypeName typeName) {
            return String.format("%s.%s", typeName.getPackage(), typeName.getName());
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class NoRecursiveTypesValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            // create mapping from object type name -> names of reference types that are fields of that type
            Multimap<TypeName, TypeName> typeToRefFields = HashMultimap.create();

            definition
                    .getTypes()
                    .forEach(type -> getReferenceType(type)
                            .ifPresent(
                                    entry -> typeToRefFields.put(type.accept(TypeDefinitionVisitor.TYPE_NAME), entry)));

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
                throw new ConjureIllegalStateException("Illegal recursive data type: "
                        + Joiner.on(" -> ").join(Lists.transform(path, TypeName::getName)));
            }

            path.add(typeName);
            for (TypeName currField : typeMap.get(typeName)) {
                verifyTypeHasNoRecursiveDefinitions(currField, typeMap, path);
            }
            path.remove(path.size() - 1);
        }
    }

    @com.google.errorprone.annotations.Immutable
    public static final class NoNestedOptionalValidator implements ConjureValidator<ConjureDefinition> {
        @Override
        public void validate(ConjureDefinition definition) {
            // create mapping for resolving reference types during validation
            Map<TypeName, TypeDefinition> definitionMap = definition.getTypes().stream()
                    .collect(Collectors.toMap(entry -> entry.accept(TypeDefinitionVisitor.TYPE_NAME), entry -> entry));
            definition.getTypes().forEach(def -> validateTypeDefinition(def, definitionMap));
            definition.getErrors().forEach(def -> validateErrorDefinition(def, definitionMap));
            definition.getServices().forEach(def -> validateServiceDefinition(def, definitionMap));
        }

        private static void validateServiceDefinition(
                ServiceDefinition serviceDef, Map<TypeName, TypeDefinition> definitionMap) {
            serviceDef.getEndpoints().forEach(endpoint -> {
                endpoint.getArgs().stream()
                        .filter(arg -> recursivelyFindNestedOptionals(arg.getType(), definitionMap, false))
                        .findAny()
                        .ifPresent(_arg -> {
                            throw new ConjureIllegalStateException(
                                    "Illegal nested optionals found in one of the arguments of endpoint "
                                            + endpoint.getEndpointName().get());
                        });
                endpoint.getReturns().ifPresent(returnType -> {
                    if (recursivelyFindNestedOptionals(returnType, definitionMap, false)) {
                        throw new ConjureIllegalStateException(
                                "Illegal nested optionals found in return type of endpoint "
                                        + endpoint.getEndpointName().get());
                    }
                });
            });
        }

        private static void validateErrorDefinition(
                ErrorDefinition errorDef, Map<TypeName, TypeDefinition> definitionMap) {
            Stream.concat(errorDef.getSafeArgs().stream(), errorDef.getUnsafeArgs().stream())
                    .filter(arg -> recursivelyFindNestedOptionals(arg.getType(), definitionMap, false))
                    .findAny()
                    .ifPresent(_arg -> {
                        throw new ConjureIllegalStateException(
                                "Illegal nested optionals found in one of arguments of error "
                                        + errorDef.getErrorName().getName());
                    });
        }

        private static void validateTypeDefinition(
                TypeDefinition typeDef, Map<TypeName, TypeDefinition> definitionMap) {

            typeDef.accept(new TypeDefinition.Visitor<Void>() {
                @Override
                public Void visitAlias(AliasDefinition _value) {
                    AliasDefinition aliasDef = typeDef.accept(TypeDefinitionVisitor.ALIAS);
                    if (recursivelyFindNestedOptionals(aliasDef.getAlias(), definitionMap, false)) {
                        throw new ConjureIllegalStateException("Illegal nested optionals found in alias "
                                + aliasDef.getTypeName().getName());
                    }
                    return null;
                }

                @Override
                public Void visitObject(ObjectDefinition _value) {
                    ObjectDefinition objectDefinition = typeDef.accept(TypeDefinitionVisitor.OBJECT);
                    objectDefinition.getFields().stream()
                            .filter(fieldDefinition ->
                                    recursivelyFindNestedOptionals(fieldDefinition.getType(), definitionMap, false))
                            .findAny()
                            .ifPresent(_found -> {
                                throw new ConjureIllegalStateException("Illegal nested optionals found in object "
                                        + objectDefinition.getTypeName().getName());
                            });
                    return null;
                }

                @Override
                public Void visitUnion(UnionDefinition _value) {
                    UnionDefinition unionDefinition = typeDef.accept(TypeDefinitionVisitor.UNION);
                    unionDefinition.getUnion().stream()
                            .filter(fieldDefinition ->
                                    recursivelyFindNestedOptionals(fieldDefinition.getType(), definitionMap, false))
                            .findAny()
                            .ifPresent(_found -> {
                                throw new ConjureIllegalStateException("Illegal nested optionals found in union "
                                        + unionDefinition.getTypeName().getName());
                            });
                    return null;
                }

                @Override
                public Void visitEnum(EnumDefinition _value) {
                    return null;
                }

                @Override
                public Void visitUnknown(String _unknownType) {
                    return null;
                }
            });
        }

        private static boolean recursivelyFindNestedOptionals(
                Type type, Map<TypeName, TypeDefinition> definitionMap, boolean isOptionalSeen) {
            if (type.accept(TypeVisitor.IS_REFERENCE)) {
                TypeDefinition referenceDefinition = definitionMap.get(type.accept(TypeVisitor.REFERENCE));
                // we only care about reference of alias type
                if (referenceDefinition != null && referenceDefinition.accept(TypeDefinitionVisitor.IS_ALIAS)) {
                    AliasDefinition aliasDef = referenceDefinition.accept(TypeDefinitionVisitor.ALIAS);
                    return recursivelyFindNestedOptionals(aliasDef.getAlias(), definitionMap, isOptionalSeen);
                }
            } else if (type.accept(TypeVisitor.IS_OPTIONAL)) {
                if (isOptionalSeen) {
                    return true;
                }
                return recursivelyFindNestedOptionals(
                        type.accept(TypeVisitor.OPTIONAL).getItemType(), definitionMap, true);
            }
            return false;
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class IllegalMapKeyValidator implements ConjureValidator<ConjureDefinition> {

        @Override
        public void validate(ConjureDefinition definition) {
            // create mapping for resolving reference types during validation
            Map<TypeName, TypeDefinition> definitionMap = definition.getTypes().stream()
                    .collect(Collectors.toMap(entry -> entry.accept(TypeDefinitionVisitor.TYPE_NAME), entry -> entry));
            definition.getTypes().forEach(def -> validateTypeDefinition(def, definitionMap));
            definition.getErrors().forEach(def -> validateErrorDefinition(def, definitionMap));
            definition.getServices().forEach(def -> validateServiceDefinition(def, definitionMap));
        }

        private static void validateServiceDefinition(
                ServiceDefinition serviceDef, Map<TypeName, TypeDefinition> definitionMap) {
            serviceDef.getEndpoints().forEach(endpoint -> {
                endpoint.getArgs().stream()
                        .filter(arg -> recursivelyFindIllegalKeys(arg.getType(), definitionMap, false))
                        .findAny()
                        .ifPresent(_arg -> {
                            throw new ConjureIllegalStateException(
                                    "Illegal map key found in one of the arguments of endpoint "
                                            + endpoint.getEndpointName().get()
                                            + ". Map keys can only be primitive Conjure types.");
                        });
                endpoint.getReturns().ifPresent(returnType -> {
                    if (recursivelyFindIllegalKeys(returnType, definitionMap, false)) {
                        throw new ConjureIllegalStateException("Illegal map key found in return type of endpoint "
                                + endpoint.getEndpointName().get() + ". Map keys can only be primitive Conjure types.");
                    }
                });
            });
        }

        private static void validateErrorDefinition(
                ErrorDefinition errorDef, Map<TypeName, TypeDefinition> definitionMap) {
            Stream.concat(errorDef.getSafeArgs().stream(), errorDef.getUnsafeArgs().stream())
                    .filter(arg -> recursivelyFindIllegalKeys(arg.getType(), definitionMap, false))
                    .findAny()
                    .ifPresent(_arg -> {
                        throw new ConjureIllegalStateException("Illegal map key found in one of arguments of error "
                                + errorDef.getErrorName().getName()
                                + ". Map keys can only be primitive Conjure types.");
                    });
        }

        private static void validateTypeDefinition(
                TypeDefinition typeDef, Map<TypeName, TypeDefinition> definitionMap) {

            typeDef.accept(new TypeDefinition.Visitor<Void>() {
                @Override
                public Void visitAlias(AliasDefinition _value) {
                    AliasDefinition aliasDef = typeDef.accept(TypeDefinitionVisitor.ALIAS);
                    if (recursivelyFindIllegalKeys(aliasDef.getAlias(), definitionMap, false)) {
                        throw new ConjureIllegalStateException("Illegal map key found in alias "
                                + aliasDef.getTypeName().getName() + ". Map keys can only be primitive Conjure types.");
                    }
                    return null;
                }

                @Override
                public Void visitObject(ObjectDefinition _value) {
                    ObjectDefinition objectDefinition = typeDef.accept(TypeDefinitionVisitor.OBJECT);
                    objectDefinition.getFields().stream()
                            .filter(fieldDefinition ->
                                    recursivelyFindIllegalKeys(fieldDefinition.getType(), definitionMap, false))
                            .findAny()
                            .ifPresent(found -> {
                                throw new ConjureIllegalStateException(String.format(
                                        "Illegal map key found in object %s in field %s. Map keys can only be"
                                                + " primitive Conjure types.",
                                        objectDefinition.getTypeName().getName(),
                                        found.getFieldName().get()));
                            });
                    return null;
                }

                @Override
                public Void visitUnion(UnionDefinition _value) {
                    UnionDefinition unionDefinition = typeDef.accept(TypeDefinitionVisitor.UNION);
                    unionDefinition.getUnion().stream()
                            .filter(fieldDefinition ->
                                    recursivelyFindIllegalKeys(fieldDefinition.getType(), definitionMap, false))
                            .findAny()
                            .ifPresent(found -> {
                                throw new ConjureIllegalStateException(String.format(
                                        "Illegal map key found in union %s in member %s. Map keys can only be"
                                                + " primitive Conjure types.",
                                        unionDefinition.getTypeName().getName(),
                                        found.getFieldName().get()));
                            });
                    return null;
                }

                @Override
                public Void visitEnum(EnumDefinition _value) {
                    return null;
                }

                @Override
                public Void visitUnknown(String _unknownType) {
                    return null;
                }
            });
        }

        private static boolean recursivelyFindIllegalKeys(
                Type type, Map<TypeName, TypeDefinition> definitionMap, boolean isMapKey) {
            if (type.accept(TypeVisitor.IS_MAP)) {
                if (isMapKey) {
                    return true;
                }
                return recursivelyFindIllegalKeys(type.accept(TypeVisitor.MAP).getKeyType(), definitionMap, true)
                        || recursivelyFindIllegalKeys(
                                type.accept(TypeVisitor.MAP).getValueType(), definitionMap, false);
            }

            if (isMapKey) {
                return new DealiasingTypeVisitor(definitionMap)
                        .dealias(type)
                        .fold(
                                typeDefinition -> !typeDefinition.accept(TypeDefinitionVisitor.IS_ENUM),
                                subType -> !subType.accept(TypeVisitor.IS_PRIMITIVE)
                                        || subType.accept(TypeVisitor.IS_ANY));
            }
            return false;
        }
    }

    @com.google.errorprone.annotations.Immutable
    private static final class LogSafetyConjureDefinitionValidator implements ConjureValidator<ConjureDefinition> {

        private final SafetyDeclarationRequirements safetyDeclarations;

        LogSafetyConjureDefinitionValidator(SafetyDeclarationRequirements safetyDeclarations) {
            this.safetyDeclarations = safetyDeclarations;
        }

        @Override
        public void validate(ConjureDefinition definition) {
            List<String> errors = new ArrayList<>();

            definition
                    .getTypes()
                    .forEach(type ->
                            SafetyValidator.validate(type, safetyDeclarations).forEach(errors::add));

            definition
                    .getServices()
                    .forEach(serviceDefinition -> serviceDefinition
                            .getEndpoints()
                            .forEach(endpointDefinition -> endpointDefinition
                                    .getArgs()
                                    .forEach(argumentDefinition -> {
                                        SafetyValidator.validateDefinition(
                                                        endpointDefinition,
                                                        argumentDefinition.getArgName(),
                                                        argumentDefinition.getSafety(),
                                                        argumentDefinition.getType(),
                                                        safetyDeclarations)
                                                .map(message -> String.format(
                                                        "%s.%s(%s): %s",
                                                        serviceDefinition
                                                                .getServiceName()
                                                                .getName(),
                                                        endpointDefinition.getEndpointName(),
                                                        argumentDefinition.getArgName(),
                                                        message))
                                                .forEach(errors::add);

                                        // In strict mode we don't allow legacy safety tags or markers
                                        if (safetyDeclarations.required()) {
                                            if (argumentDefinition.getTags().contains("safe")
                                                    || argumentDefinition
                                                            .getTags()
                                                            .contains("unsafe")) {
                                                errors.add(String.format(
                                                        "%s.%s(%s): Safety tags have been replaced by the 'safety'"
                                                            + " field and are no longer allowed when 'requireSafety'"
                                                            + " is enabled",
                                                        serviceDefinition
                                                                .getServiceName()
                                                                .getName(),
                                                        endpointDefinition.getEndpointName(),
                                                        argumentDefinition.getArgName()));
                                            }
                                            if (argumentDefinition.getMarkers().stream()
                                                    .anyMatch(LogSafetyConjureDefinitionValidator::isSafetyMarker)) {
                                                errors.add(String.format(
                                                        "%s.%s(%s): Safety markers have been replaced by the 'safety'"
                                                            + " field and are no longer allowed when 'requireSafety'"
                                                            + " is enabled",
                                                        serviceDefinition
                                                                .getServiceName()
                                                                .getName(),
                                                        endpointDefinition.getEndpointName(),
                                                        argumentDefinition.getArgName()));
                                            }
                                        }
                                    })));

            if (!errors.isEmpty()) {
                throw new ConjureIllegalStateException(String.join("\n", errors));
            }
        }

        private static boolean isSafetyMarker(Type type) {
            if (type.accept(TypeVisitor.IS_REFERENCE)) {
                TypeName marker = type.accept(TypeVisitor.REFERENCE);
                return "com.palantir.logsafe".equals(marker.getPackage())
                        && ("Safe".equals(marker.getName()) || "Unsafe".equals(marker.getName()));
            }
            return false;
        }
    }

    @com.google.errorprone.annotations.Immutable
    private enum UniqueErrorNameValidator implements ConjureValidator<ConjureDefinition> {
        INSTANCE;

        @Override
        public void validate(ConjureDefinition definition) {
            ListMultimap<String, ErrorDefinition> errors = definition.getErrors().stream()
                    .collect(Multimaps.toMultimap(
                            errorDefinition -> errorDefinition.getNamespace().get()
                                    + ':'
                                    + errorDefinition.getErrorName().getName(),
                            errorDefinition -> errorDefinition,
                            ArrayListMultimap::create));

            Map<String, Collection<ErrorDefinition>> duplicateErrors =
                    Maps.filterValues(errors.asMap(), value -> value.size() > 1);
            if (!duplicateErrors.isEmpty()) {
                throw new IllegalStateException("Error names are made up of a namespace and name, "
                        + "used to uniquely identify error types over the wire. "
                        + "The following errors are defined multiple times:\n"
                        + duplicateErrors.entrySet().stream()
                                .map(entry ->
                                        String.format("'%s': %s", entry.getKey(), describeDuplicates(entry.getValue())))
                                .collect(Collectors.joining("\n")));
            }
        }

        private static String describeDuplicates(Collection<ErrorDefinition> definitions) {
            List<String> packages = definitions.stream()
                    .map(errorDef -> errorDef.getErrorName().getPackage())
                    .distinct()
                    .toList();
            if (definitions.size() == packages.size()) {
                return packages.stream().collect(Collectors.joining(", ", "in packages: ", ""));
            }
            return definitions.stream()
                    .map(ErrorDefinition::toString)
                    .collect(
                            Collectors.joining("\n\t", String.format("defined %s times:\n\t", definitions.size()), ""));
        }
    }
}

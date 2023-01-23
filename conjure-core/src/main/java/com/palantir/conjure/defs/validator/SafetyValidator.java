/*
 * (c) Copyright 2022 Palantir Technologies Inc. All rights reserved.
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

import com.palantir.conjure.defs.SafetyDeclarationRequirements;
import com.palantir.conjure.exceptions.ConjureIllegalStateException;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.FieldName;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.LogSafety;
import com.palantir.conjure.spec.MapType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.spec.UnionDefinition;
import java.util.Optional;
import java.util.stream.Stream;

public final class SafetyValidator {
    private static TypeDefinition.Visitor<Stream<String>> safetyVisitor;

    private static TypeSafetyAllowedVisitor safetyAllowedVisitor;

    private SafetyValidator() {}

    public static Stream<String> validate(TypeDefinition type, SafetyDeclarationRequirements safetyDeclarations) {
        setupVisitors(safetyDeclarations);
        return type.accept(safetyVisitor);
    }

    public static Stream<String> validateDefinition(
            EndpointDefinition endpointDefinition,
            ArgumentName argumentName,
            Optional<LogSafety> declaredSafety,
            Type type,
            SafetyDeclarationRequirements safetyDeclarations) {
        setupVisitors(safetyDeclarations);
        String errorMessage = String.format(
                "Endpoint %s argument %s must declare log safety using 'safety: VALUE' "
                        + "where VALUE may be safe, unsafe, or do-not-log.",
                endpointDefinition.getEndpointName(), argumentName);
        return validateInternal(
                type, declaredSafety, endpointDefinition.getEndpointName().get(), errorMessage, safetyDeclarations);
    }

    private static void setupVisitors(SafetyDeclarationRequirements safetyDeclarations) {
        safetyVisitor = new TypeDefinitionSafetyVisitor(safetyDeclarations);
        safetyAllowedVisitor = new TypeSafetyAllowedVisitor(safetyDeclarations);
    }

    private static Stream<String> validateInternal(
            Type type,
            Optional<LogSafety> declaredSafety,
            String typeReference,
            String errorMessage,
            SafetyDeclarationRequirements safetyDeclarations) {
        Optional<LogSafety> safetyToEvaluate = declaredSafety;
        Optional<ExternalReference> maybeExternalType = type.accept(ExternalReferenceVisitor.INSTANCE);
        if (maybeExternalType.isPresent()) {
            if (declaredSafety.isPresent()) {
                return Stream.of("External types must declare safety at import time, not at usage time.");
            }
            safetyToEvaluate = maybeExternalType.get().getSafety();
        }
        if (safetyToEvaluate.isPresent()) {
            return type.accept(new SafetyTypeVisitor(typeReference));
        } else if (safetyDeclarations.strictMode() && type.accept(safetyAllowedVisitor)) {
            return Stream.of(errorMessage);
        } else {
            return Stream.empty();
        }
    }

    private static String fail(String parentReference, TypeName nonPrimitiveType) {
        return String.format(
                "%s cannot declare log safety. Only conjure primitives and "
                        + "wrappers around conjure primitives may declare safety. %s.%s is not a primitive type.",
                parentReference, nonPrimitiveType.getPackage(), nonPrimitiveType.getName());
    }

    private static String getImportType(ExternalReference value) {
        return value.getExternalReference().getPackage() + "."
                + value.getExternalReference().getName();
    }

    private static final class TypeDefinitionSafetyVisitor implements TypeDefinition.Visitor<Stream<String>> {
        private final SafetyDeclarationRequirements safetyRequirements;

        TypeDefinitionSafetyVisitor(SafetyDeclarationRequirements safetyRequirements) {
            this.safetyRequirements = safetyRequirements;
        }

        @Override
        public Stream<String> visitAlias(AliasDefinition value) {
            return validateType(value.getAlias(), value.getSafety(), qualifyTypeReference(value.getTypeName()));
        }

        @Override
        public Stream<String> visitEnum(EnumDefinition _value) {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitObject(ObjectDefinition value) {
            return value.getFields().stream()
                    .flatMap(field -> validateType(
                            field.getType(),
                            field.getSafety(),
                            qualifyTypeReference(value.getTypeName(), field.getFieldName())));
        }

        @Override
        public Stream<String> visitUnion(UnionDefinition value) {
            return value.getUnion().stream()
                    .flatMap(field -> validateType(
                            field.getType(),
                            field.getSafety(),
                            qualifyTypeReference(value.getTypeName(), field.getFieldName())));
        }

        @Override
        public Stream<String> visitUnknown(String unknownType) {
            throw new ConjureIllegalStateException("Unknown type: " + unknownType);
        }

        private Stream<String> validateType(
                Type type, Optional<LogSafety> declaredSafety, String qualifiedTypeReference) {
            String errorMessage = qualifiedTypeReference + " must declare log safety using 'safety: VALUE' "
                    + "where VALUE may be safe, unsafe, or do-not-log.";
            return validateInternal(type, declaredSafety, qualifiedTypeReference, errorMessage, safetyRequirements);
        }

        private static String qualifyTypeReference(TypeName typeName, FieldName fieldName) {
            return typeName.getPackage() + '.' + typeName.getName() + "::" + fieldName;
        }

        private static String qualifyTypeReference(TypeName typeName) {
            return typeName.getPackage() + '.' + typeName.getName();
        }
    }

    /** Validates elements which declare safety. Fails if any non-primitive is referenced. */
    private static final class SafetyTypeVisitor implements Type.Visitor<Stream<String>> {

        private final String parentReference;

        SafetyTypeVisitor(String parentReference) {
            this.parentReference = parentReference;
        }

        @Override
        public Stream<String> visitPrimitive(PrimitiveType value) {
            return value.accept(PrimitiveTypeSafetyVisitor.INSTANCE);
        }

        @Override
        public Stream<String> visitOptional(OptionalType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Stream<String> visitList(ListType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Stream<String> visitSet(SetType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Stream<String> visitMap(MapType map) {
            return Stream.of("Maps cannot declare log safety. Consider using alias types for keys or values to "
                    + "leverage the type system. Failing map: "
                    + map);
        }

        @Override
        public Stream<String> visitReference(TypeName value) {
            return Stream.of(fail(parentReference, value));
        }

        @Override
        public Stream<String> visitExternal(ExternalReference value) {
            String importType = getImportType(value);
            if (ValidImportsForSafety.ALLOWED_IMPORTS.containsKey(importType)) {
                Type givenFallbackType =
                        Type.primitive(PrimitiveType.valueOf(ValidImportsForSafety.ALLOWED_IMPORTS.get(importType)));
                if (givenFallbackType.equals(value.getFallback())) {
                    return Stream.empty();
                } else {
                    return Stream.of(String.format(
                            "Mismatched base type. %s must have a base type of %s in order to declare safety.",
                            importType, ValidImportsForSafety.ALLOWED_IMPORTS.get(importType)));
                }
            }
            return Stream.of(fail(parentReference, value.getExternalReference()));
        }

        @Override
        public Stream<String> visitUnknown(String unknownType) {
            throw new ConjureIllegalStateException("Unknown type: " + unknownType);
        }
    }

    /**
     * Validates elements which declare safety. Fails if any non-primitive is referenced.
     * Ensures bearer-token safety cannot be overridden from {@code do-not-log}.
     */
    private enum PrimitiveTypeSafetyVisitor implements PrimitiveType.Visitor<Stream<String>> {
        INSTANCE;

        @Override
        public Stream<String> visitString() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitDatetime() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitInteger() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitDouble() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitSafelong() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitBinary() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitAny() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitBoolean() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitUuid() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitRid() {
            return Stream.empty();
        }

        @Override
        public Stream<String> visitBearertoken() {
            return Stream.of("bearertoken values are do-not-log by default and cannot be configured");
        }

        @Override
        public Stream<String> visitUnknown(String unknownValue) {
            throw new ConjureIllegalStateException("Unknown primitive type: " + unknownValue);
        }
    }

    private static final class TypeSafetyAllowedVisitor implements Type.Visitor<Boolean> {

        private boolean externalImportsRequired;

        TypeSafetyAllowedVisitor(SafetyDeclarationRequirements requirements) {
            this.externalImportsRequired = requirements.external_imports_required();
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType value) {
            return value.accept(PrimitiveTypeSafetyAllowedVisitor.INSTANCE);
        }

        @Override
        public Boolean visitOptional(OptionalType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Boolean visitList(ListType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Boolean visitSet(SetType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Boolean visitMap(MapType _map) {
            return false;
        }

        @Override
        public Boolean visitReference(TypeName _value) {
            return false;
        }

        @Override
        public Boolean visitExternal(ExternalReference value) {
            String importType = getImportType(value);
            return externalImportsRequired && ValidImportsForSafety.ALLOWED_IMPORTS.containsKey(importType);
        }

        @Override
        public Boolean visitUnknown(String unknownType) {
            throw new ConjureIllegalStateException("Unknown type: " + unknownType);
        }
    }

    /** Returns whether the type allows safety information. */
    private enum PrimitiveTypeSafetyAllowedVisitor implements PrimitiveType.Visitor<Boolean> {
        INSTANCE;

        @Override
        public Boolean visitString() {
            return true;
        }

        @Override
        public Boolean visitDatetime() {
            return true;
        }

        @Override
        public Boolean visitInteger() {
            return true;
        }

        @Override
        public Boolean visitDouble() {
            return true;
        }

        @Override
        public Boolean visitSafelong() {
            return true;
        }

        @Override
        public Boolean visitBinary() {
            return true;
        }

        @Override
        public Boolean visitAny() {
            return true;
        }

        @Override
        public Boolean visitBoolean() {
            return true;
        }

        @Override
        public Boolean visitUuid() {
            return true;
        }

        @Override
        public Boolean visitRid() {
            return true;
        }

        @Override
        public Boolean visitBearertoken() {
            return false;
        }

        @Override
        public Boolean visitUnknown(String unknownValue) {
            throw new ConjureIllegalStateException("Unknown primitive type: " + unknownValue);
        }
    }
}

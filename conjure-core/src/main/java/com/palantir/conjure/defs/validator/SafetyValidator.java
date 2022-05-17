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

public final class SafetyValidator {
    private static final TypeDefinition.Visitor<Void> SAFETY_VISITOR_DECLARATIONS_ALLOWED =
            new TypeDefinitionSafetyVisitor(SafetyDeclarationRequirements.ALLOWED);
    private static final TypeDefinition.Visitor<Void> SAFETY_VISITOR_DECLARATIONS_REQUIRED =
            new TypeDefinitionSafetyVisitor(SafetyDeclarationRequirements.REQUIRED);

    private SafetyValidator() {}

    public static void validate(TypeDefinition type, SafetyDeclarationRequirements safetyDeclarations) {
        type.accept(
                safetyDeclarations.required()
                        ? SAFETY_VISITOR_DECLARATIONS_REQUIRED
                        : SAFETY_VISITOR_DECLARATIONS_ALLOWED);
    }

    public static void validateDefinition(
            EndpointDefinition endpointDefinition,
            ArgumentName argumentName,
            Optional<LogSafety> declaredSafety,
            Type type,
            SafetyDeclarationRequirements safetyDeclarations) {
        if (declaredSafety.isPresent()) {
            type.accept(
                    new SafetyTypeVisitor(endpointDefinition.getEndpointName().get()));
        } else if (safetyDeclarations.required() && type.accept(TypeSafetyAllowedVisitor.INSTANCE)) {
            throw new ConjureIllegalStateException(String.format(
                    "Endpoint %s argument %s must declare log safety using 'safety: VALUE' "
                            + "where VALUE may be safe, unsafe, or do-not-log.",
                    endpointDefinition.getEndpointName(), argumentName));
        }
    }

    private static ConjureIllegalStateException fail(String parentReference, TypeName nonPrimitiveType) {
        return new ConjureIllegalStateException(String.format(
                "%s cannot declare log safety. Only conjure primitives and "
                        + "wrappers around conjure primitives may declare safety. %s.%s is not a primitive type.",
                parentReference, nonPrimitiveType.getPackage(), nonPrimitiveType.getName()));
    }

    private static final class TypeDefinitionSafetyVisitor implements TypeDefinition.Visitor<Void> {
        private final SafetyDeclarationRequirements safetyRequirements;

        TypeDefinitionSafetyVisitor(SafetyDeclarationRequirements safetyRequirements) {
            this.safetyRequirements = safetyRequirements;
        }

        @Override
        public Void visitAlias(AliasDefinition value) {
            validateType(value.getAlias(), value.getSafety(), qualifyTypeReference(value.getTypeName()));
            return null;
        }

        @Override
        public Void visitEnum(EnumDefinition _value) {
            return null;
        }

        @Override
        public Void visitObject(ObjectDefinition value) {
            value.getFields()
                    .forEach(field -> validateType(
                            field.getType(),
                            field.getSafety(),
                            qualifyTypeReference(value.getTypeName(), field.getFieldName())));
            return null;
        }

        @Override
        public Void visitUnion(UnionDefinition value) {
            value.getUnion()
                    .forEach(field -> validateType(
                            field.getType(),
                            field.getSafety(),
                            qualifyTypeReference(value.getTypeName(), field.getFieldName())));
            return null;
        }

        @Override
        public Void visitUnknown(String unknownType) {
            throw new ConjureIllegalStateException("Unknown type: " + unknownType);
        }

        private void validateType(Type type, Optional<LogSafety> declaredSafety, String qualifiedTypeReference) {
            if (declaredSafety.isPresent()) {
                type.accept(new SafetyTypeVisitor(qualifiedTypeReference));
            } else if (safetyRequirements.required() && type.accept(TypeSafetyAllowedVisitor.INSTANCE)) {
                throw new ConjureIllegalStateException(
                        qualifiedTypeReference + " must declare log safety using 'safety: VALUE' "
                                + "where VALUE may be safe, unsafe, or do-not-log.");
            }
        }

        private static String qualifyTypeReference(TypeName typeName, FieldName fieldName) {
            return typeName.getPackage() + '.' + typeName.getName() + "::" + fieldName;
        }

        private static String qualifyTypeReference(TypeName typeName) {
            return typeName.getPackage() + '.' + typeName.getName();
        }
    }

    /** Validates elements which declare safety. Fails if any non-primitive is referenced. */
    private static final class SafetyTypeVisitor implements Type.Visitor<Void> {

        private final String parentReference;

        SafetyTypeVisitor(String parentReference) {
            this.parentReference = parentReference;
        }

        @Override
        public Void visitPrimitive(PrimitiveType value) {
            value.accept(PrimitiveTypeSafetyVisitor.INSTANCE);
            return null;
        }

        @Override
        public Void visitOptional(OptionalType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Void visitList(ListType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Void visitSet(SetType value) {
            return value.getItemType().accept(this);
        }

        @Override
        public Void visitMap(MapType map) {
            throw new ConjureIllegalStateException(
                    "Maps cannot declare log safety. Consider using alias types for keys or values to "
                            + "leverage the type system. Failing map: "
                            + map);
        }

        @Override
        public Void visitReference(TypeName value) {
            throw fail(parentReference, value);
        }

        @Override
        public Void visitExternal(ExternalReference value) {
            throw fail(parentReference, value.getExternalReference());
        }

        @Override
        public Void visitUnknown(String unknownType) {
            throw new ConjureIllegalStateException("Unknown type: " + unknownType);
        }
    }

    /**
     * Validates elements which declare safety. Fails if any non-primitive is referenced.
     * Ensures bearer-token safety cannot be overridden from {@code do-not-log}.
     */
    private enum PrimitiveTypeSafetyVisitor implements PrimitiveType.Visitor<Void> {
        INSTANCE;

        @Override
        public Void visitString() {
            return null;
        }

        @Override
        public Void visitDatetime() {
            return null;
        }

        @Override
        public Void visitInteger() {
            return null;
        }

        @Override
        public Void visitDouble() {
            return null;
        }

        @Override
        public Void visitSafelong() {
            return null;
        }

        @Override
        public Void visitBinary() {
            return null;
        }

        @Override
        public Void visitAny() {
            return null;
        }

        @Override
        public Void visitBoolean() {
            return null;
        }

        @Override
        public Void visitUuid() {
            return null;
        }

        @Override
        public Void visitRid() {
            return null;
        }

        @Override
        public Void visitBearertoken() {
            throw new ConjureIllegalStateException(
                    "bearertoken values are do-not-log by default and cannot be configured");
        }

        @Override
        public Void visitUnknown(String unknownValue) {
            throw new ConjureIllegalStateException("Unknown primitive type: " + unknownValue);
        }
    }

    private enum TypeSafetyAllowedVisitor implements Type.Visitor<Boolean> {
        INSTANCE;

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
        public Boolean visitExternal(ExternalReference _value) {
            return false;
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

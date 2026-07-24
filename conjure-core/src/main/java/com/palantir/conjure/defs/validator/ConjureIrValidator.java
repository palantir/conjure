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

import com.palantir.conjure.defs.SafetyDeclarationRequirements;
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ConjureDefinition;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.FieldDefinition;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.UnionDefinition;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import com.palantir.conjure.visitor.TypeDefinitionVisitor;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Validates an already-deserialized {@link ConjureDefinition} the way the compiler validates during parse.
 *
 * <p>{@link ConjureDefinitionValidator#validateAll} runs only cross-cutting checks (unique names, version,
 * recursion, nested optionals, illegal map keys, log-safety). The per-type identifier and format validators are
 * otherwise invoked only while parsing source. This entry point runs both, so a consumer that deserializes compiled
 * IR (rather than parsing source) can enforce the same spec conformance.
 */
public final class ConjureIrValidator {

    private ConjureIrValidator() {}

    /** Validates using {@link SafetyDeclarationRequirements#ALLOWED}. */
    public static void validate(ConjureDefinition definition) {
        validate(definition, SafetyDeclarationRequirements.ALLOWED);
    }

    public static void validate(ConjureDefinition definition, SafetyDeclarationRequirements safetyRequirements) {
        DealiasingTypeVisitor dealiasingTypeVisitor = new DealiasingTypeVisitor(definition.getTypes().stream()
                .collect(Collectors.toMap(type -> type.accept(TypeDefinitionVisitor.TYPE_NAME), Function.identity())));

        definition.getTypes().forEach(type -> type.accept(TypeValidator.INSTANCE));
        definition.getErrors().forEach(error -> {
            PackageValidator.validate(error.getErrorName().getPackage());
            // Field names/definitions must be validated before ErrorDefinitionValidator, which normalizes field
            // names via FieldNameValidator#toCase and assumes they already conform to the FieldName grammar.
            validateFields(error.getSafeArgs());
            validateFields(error.getUnsafeArgs());
            ErrorDefinitionValidator.validate(error);
        });
        definition.getServices().forEach(service -> {
            PackageValidator.validate(service.getServiceName().getPackage());
            ServiceDefinitionValidator.validateAll(service);
            service.getEndpoints().forEach(endpoint -> {
                HttpPathValidator.validate(endpoint.getHttpPath());
                EndpointDefinitionValidator.validateAll(endpoint, dealiasingTypeVisitor);
            });
        });

        ConjureDefinitionValidator.validateAll(definition, safetyRequirements);
    }

    /** Validates each field's name and definition, matching the checks the parser applies via its shared helper. */
    private static void validateFields(List<FieldDefinition> fields) {
        fields.forEach(field -> {
            FieldNameValidator.validate(field.getFieldName());
            FieldDefinitionValidator.validate(field);
        });
    }

    private enum TypeValidator implements TypeDefinition.Visitor<Void> {
        INSTANCE;

        @Override
        public Void visitAlias(AliasDefinition value) {
            PackageValidator.validate(value.getTypeName().getPackage());
            TypeNameValidator.validate(value.getTypeName());
            return null;
        }

        @Override
        public Void visitEnum(EnumDefinition value) {
            PackageValidator.validate(value.getTypeName().getPackage());
            TypeNameValidator.validate(value.getTypeName());
            EnumDefinitionValidator.validateAll(value);
            return null;
        }

        @Override
        public Void visitObject(ObjectDefinition value) {
            PackageValidator.validate(value.getTypeName().getPackage());
            TypeNameValidator.validate(value.getTypeName());
            // Field names/definitions must be validated before ObjectDefinitionValidator, which normalizes field
            // names via FieldNameValidator#toCase and assumes they already conform to the FieldName grammar.
            validateFields(value.getFields());
            ObjectDefinitionValidator.validate(value);
            return null;
        }

        @Override
        public Void visitUnion(UnionDefinition value) {
            PackageValidator.validate(value.getTypeName().getPackage());
            TypeNameValidator.validate(value.getTypeName());
            validateFields(value.getUnion());
            UnionDefinitionValidator.validateAll(value);
            return null;
        }

        @Override
        public Void visitUnknown(String _unknownType) {
            return null;
        }
    }
}

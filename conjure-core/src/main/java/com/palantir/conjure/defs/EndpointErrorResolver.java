/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.defs;

import com.google.common.base.Preconditions;
import com.palantir.conjure.parser.AnnotatedConjureSourceFile;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.builtin.DateTimeType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.conjure.spec.ErrorTypeName;
import com.palantir.logsafe.SafeArg;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoint error definitions are either references to errors defined in the same file as the endpoint definition, or in
 * imported Conjure files. This class creates a {@link com.palantir.conjure.spec.ErrorTypeName} object from a
 * {@link com.palantir.conjure.parser.types.reference.ReferenceType} to a Conjure defined error definition.
 */
final class EndpointErrorResolver implements ConjureTypeVisitor<ErrorTypeName> {
    private static final String UNSUPPORTED_TYPE_MESSAGE =
            "Unsupported endpoint error type. Endpoint errors must be references to a Conjure-defined error type";

    private final Map<TypeName, ErrorTypeDefinition> parsedErrors;
    private final Optional<ConjurePackage> defaultConjurePackage;
    private final Map<Namespace, String> importProviders;
    private final Map<String, AnnotatedConjureSourceFile> externalTypes;

    EndpointErrorResolver(
            Map<TypeName, ErrorTypeDefinition> parsedErrors,
            Optional<ConjurePackage> defaultConjurePackage,
            Map<Namespace, String> importProviders,
            Map<String, AnnotatedConjureSourceFile> externalTypes) {
        this.parsedErrors = parsedErrors;
        this.defaultConjurePackage = defaultConjurePackage;
        this.importProviders = importProviders;
        this.externalTypes = externalTypes;
    }

    ErrorTypeName resolve(ConjureType conjureType) {
        return conjureType.visit(this);
    }

    private ErrorTypeName resolveReferenceType(LocalReferenceType localReferenceType) {
        return resolveInternal(localReferenceType.type(), parsedErrors, defaultConjurePackage);
    }

    private ErrorTypeName resolveReferenceType(ForeignReferenceType foreignReferenceType) {
        String namespaceFile = importProviders.get(foreignReferenceType.namespace());
        Preconditions.checkNotNull(
                namespaceFile, "Import not found for namespace: %s", foreignReferenceType.namespace());
        AnnotatedConjureSourceFile externalFile = externalTypes.get(namespaceFile);
        Preconditions.checkNotNull(
                externalFile, "File not found for namespace: %s @ %s", foreignReferenceType.namespace(), namespaceFile);
        return resolveInternal(
                foreignReferenceType.type(),
                externalFile.conjureSourceFile().types().definitions().errors(),
                externalFile.conjureSourceFile().types().definitions().defaultConjurePackage());
    }

    private static ErrorTypeName resolveInternal(
            TypeName name,
            Map<TypeName, ErrorTypeDefinition> parsedErrorDefinitions,
            Optional<ConjurePackage> defaultConjurePackage) {
        ErrorTypeDefinition errorDefinition = parsedErrorDefinitions.get(name);
        if (errorDefinition == null) {
            throw new SafeIllegalArgumentException("Unknown error", SafeArg.of("error", name.name()));
        }
        return ErrorTypeName.builder()
                .name(name.name())
                .package_(ConjureParserUtils.parsePackageOrElseThrow(
                        errorDefinition.conjurePackage(),
                        defaultConjurePackage.map(ConjureParserUtils::parseConjurePackage)))
                .namespace(com.palantir.conjure.spec.ErrorNamespace.of(
                        errorDefinition.namespace().name()))
                .build();
    }

    @Override
    public ErrorTypeName visitAny(AnyType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitList(ListType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitMap(MapType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitOptional(OptionalType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitPrimitive(PrimitiveType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitLocalReference(LocalReferenceType type) {
        return resolveReferenceType(type);
    }

    @Override
    public ErrorTypeName visitForeignReference(ForeignReferenceType type) {
        return resolveReferenceType(type);
    }

    @Override
    public ErrorTypeName visitSet(SetType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitBinary(BinaryType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }

    @Override
    public ErrorTypeName visitDateTime(DateTimeType type) {
        throw new SafeIllegalArgumentException(UNSUPPORTED_TYPE_MESSAGE, SafeArg.of("type", type));
    }
}

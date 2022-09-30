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

package com.palantir.conjure.defs;

import com.google.common.base.Preconditions;
import com.palantir.conjure.exceptions.ConjureIllegalStateException;
import com.palantir.conjure.parser.AnnotatedConjureSourceFile;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.builtin.DateTimeType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import java.util.Map;
import java.util.Optional;

/** The core translator between parsed/raw types and the IR spec representation exposed to compilers. */
public final class ConjureTypeParserVisitor implements ConjureTypeVisitor<Type> {

    public interface ReferenceTypeResolver {
        Type resolve(LocalReferenceType reference);

        Type resolve(ForeignReferenceType reference);
    }

    // TODO(rfink): Add explicit test coverage
    public static final class ByParsedRepresentationTypeNameResolver implements ReferenceTypeResolver {

        private final TypesDefinition types;
        private final Map<Namespace, String> importProviders;
        private final Map<String, AnnotatedConjureSourceFile> externalTypes;

        public ByParsedRepresentationTypeNameResolver(
                TypesDefinition types,
                Map<Namespace, String> importProviders,
                Map<String, AnnotatedConjureSourceFile> externalTypes) {
            this.types = types;
            this.importProviders = importProviders;
            this.externalTypes = externalTypes;
        }

        @Override
        public Type resolve(LocalReferenceType reference) {
            return resolveFromTypeName(reference.type(), types);
        }

        @Override
        public Type resolve(ForeignReferenceType reference) {
            String namespaceFile = importProviders.get(reference.namespace());
            Preconditions.checkNotNull(namespaceFile, "Import not found for namespace: %s", reference.namespace());
            AnnotatedConjureSourceFile externalFile = externalTypes.get(namespaceFile);
            Preconditions.checkNotNull(
                    externalFile, "File not found for namespace: %s @ %s", reference.namespace(), namespaceFile);
            return resolveFromTypeName(
                    reference.type(), externalFile.conjureSourceFile().types());
        }

        private static Type resolveFromTypeName(
                com.palantir.conjure.parser.types.names.TypeName name, TypesDefinition types) {
            Optional<String> defaultPackage =
                    types.definitions().defaultConjurePackage().map(ConjureParserUtils::parseConjurePackage);
            BaseObjectTypeDefinition maybeDirectDef =
                    types.definitions().objects().get(name);
            String conjurePackage;
            String typeName;
            if (maybeDirectDef == null) {
                ExternalTypeDefinition maybeExternalDef = types.imports().get(name);
                if (maybeExternalDef == null) {
                    throw new ConjureIllegalStateException("Unknown LocalReferenceType: " + name);
                }

                String externalPath = maybeExternalDef.external().java();
                int lastIndex = externalPath.lastIndexOf(".");
                conjurePackage = externalPath.substring(0, lastIndex);
                typeName = externalPath.substring(lastIndex + 1);

                return Type.external(ExternalReference.builder()
                        .externalReference(TypeName.of(typeName, conjurePackage))
                        .fallback(ConjureParserUtils.parsePrimitiveType(maybeExternalDef.baseType()))
                        .safety(maybeExternalDef.safety().map(ConjureParserUtils::parseLogSafety))
                        .build());
            } else {
                // Conjure-defined object
                conjurePackage =
                        ConjureParserUtils.parsePackageOrElseThrow(maybeDirectDef.conjurePackage(), defaultPackage);
                return Type.reference(TypeName.of(name.name(), conjurePackage));
            }
        }
    }

    private final ReferenceTypeResolver nameResolver;

    public ConjureTypeParserVisitor(ReferenceTypeResolver nameResolver) {
        this.nameResolver = nameResolver;
    }

    @Override
    public Type visitAny(AnyType _type) {
        return Type.primitive(com.palantir.conjure.spec.PrimitiveType.ANY);
    }

    @Override
    public Type visitList(ListType type) {
        return Type.list(com.palantir.conjure.spec.ListType.of(type.itemType().visit(this)));
    }

    @Override
    public Type visitMap(MapType type) {
        return Type.map(com.palantir.conjure.spec.MapType.of(
                type.keyType().visit(this), type.valueType().visit(this)));
    }

    @Override
    public Type visitOptional(OptionalType type) {
        return Type.optional(
                com.palantir.conjure.spec.OptionalType.of(type.itemType().visit(this)));
    }

    @Override
    public Type visitPrimitive(PrimitiveType type) {
        return ConjureParserUtils.parsePrimitiveType(type);
    }

    @Override
    public Type visitLocalReference(LocalReferenceType type) {
        return nameResolver.resolve(type);
    }

    @Override
    public Type visitForeignReference(ForeignReferenceType type) {
        return nameResolver.resolve(type);
    }

    @Override
    public Type visitSet(SetType type) {
        return Type.set(com.palantir.conjure.spec.SetType.of(type.itemType().visit(this)));
    }

    @Override
    public Type visitBinary(BinaryType _type) {
        return Type.primitive(com.palantir.conjure.spec.PrimitiveType.BINARY);
    }

    @Override
    public Type visitDateTime(DateTimeType _type) {
        return Type.primitive(com.palantir.conjure.spec.PrimitiveType.DATETIME);
    }
}

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
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.ConjureImports;
import com.palantir.conjure.parser.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.conjure.spec.ExternalReference;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.TypeVisitor;
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

        public ByParsedRepresentationTypeNameResolver(TypesDefinition types) {
            this.types = types;
        }

        @Override
        public Type resolve(LocalReferenceType reference) {
            return resolveFromTypeName(reference.type(), types);
        }

        @Override
        public Type resolve(ForeignReferenceType reference) {
            ConjureImports conjureImports = types.conjureImports().get(reference.namespace());
            Preconditions.checkNotNull(conjureImports, "Import not found for namespace: %s", reference.namespace());
            return resolveFromTypeName(reference.type(), conjureImports.conjure().types());
        }

        private static Type resolveFromTypeName(
                com.palantir.conjure.parser.types.names.TypeName name, TypesDefinition types) {
            Optional<String> defaultPackage =
                    types.definitions().defaultConjurePackage().map(ConjureParserUtils::parseConjurePackage);
            BaseObjectTypeDefinition maybeDirectDef = types.definitions().objects().get(name);
            String conjurePackage;
            String typeName;
            if (maybeDirectDef == null) {
                ExternalTypeDefinition maybeExternalDef = types.imports().get(name);
                if (maybeExternalDef == null) {
                    throw new IllegalStateException("Unknown LocalReferenceType: " + name);
                }

                // External import
                Optional<String> externalPath = maybeExternalDef.external().values().stream().findFirst();

                if (!externalPath.isPresent()) {
                    throw new IllegalStateException("Unknown export type: " + name);
                }

                int lastIndex = externalPath.get().lastIndexOf(".");
                conjurePackage = externalPath.get().substring(0, lastIndex);
                typeName = externalPath.get().substring(lastIndex + 1);

                return Type.external(ExternalReference.builder()
                        .externalReference(TypeName.of(typeName, conjurePackage))
                        .fallback(ConjureParserUtils.parsePrimitiveType(maybeExternalDef.baseType()))
                        .build());
            } else {
                // Conjure-defined object
                conjurePackage = ConjureParserUtils.parsePackageOrElseThrow(
                        maybeDirectDef.conjurePackage(), defaultPackage);
                return Type.reference(TypeName.of(name.name(), conjurePackage));
            }
        }
    }

    private final ReferenceTypeResolver nameResolver;

    public ConjureTypeParserVisitor(ReferenceTypeResolver nameResolver) {
        this.nameResolver = nameResolver;
    }

    @Override
    public Type visitAny(AnyType type) {
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
        com.palantir.conjure.spec.OptionalType innerType = com.palantir.conjure.spec.OptionalType.of(
                type.itemType().visit(this));
        Preconditions.checkState(!innerType.getItemType().accept(TypeVisitor.IS_OPTIONAL), "Illegal nested optionals");
        return Type.optional(innerType);
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
    public Type visitBinary(BinaryType type) {
        return Type.primitive(com.palantir.conjure.spec.PrimitiveType.BINARY);
    }

    @Override
    public Type visitDateTime(DateTimeType type) {
        return Type.primitive(com.palantir.conjure.spec.PrimitiveType.DATETIME);
    }
}

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

import com.palantir.conjure.defs.ConjureTypeParserVisitor.ReferenceTypeResolver;
import com.palantir.conjure.either.Either;
import com.palantir.conjure.exceptions.ConjureIllegalArgumentException;
import com.palantir.conjure.java.lib.SafeLong;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import com.palantir.conjure.parser.types.builtin.AnyType;
import com.palantir.conjure.parser.types.builtin.BinaryType;
import com.palantir.conjure.parser.types.builtin.DateTimeType;
import com.palantir.conjure.parser.types.collect.ListType;
import com.palantir.conjure.parser.types.collect.MapType;
import com.palantir.conjure.parser.types.collect.OptionalType;
import com.palantir.conjure.parser.types.collect.SetType;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.EnumValueDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import com.palantir.conjure.parser.types.reference.ForeignReferenceType;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import com.palantir.conjure.spec.DefaultValue;
import com.palantir.ri.ResourceIdentifier;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DefaultValueParserVisitor implements ConjureTypeVisitor<DefaultValue> {
    private static final TypeDefinitionExtractor DEFINITION_EXTRACTOR = new TypeDefinitionExtractor();
    private final ReferenceTypeResolver typeResolver;
    private final String value;

    public DefaultValueParserVisitor(ReferenceTypeResolver typeResolver, String value) {
        this.typeResolver = typeResolver;
        this.value = value;
    }

    @Override
    public DefaultValue visitAny(AnyType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'any' do not support default values");
    }

    @Override
    public DefaultValue visitList(ListType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'list' do not support default values");
    }

    @Override
    public DefaultValue visitMap(MapType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'map' do not support default values");
    }

    @Override
    public DefaultValue visitOptional(OptionalType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'optional' do not support default values");
    }

    @Override
    public DefaultValue visitPrimitive(PrimitiveType type) {
        switch (type) {
            case STRING:
                return DefaultValue.string(value);
            case RID:
                return DefaultValue.rid(ResourceIdentifier.valueOf(value));
            case UUID:
                return DefaultValue.uuid(UUID.fromString(value));
            case INTEGER:
                return DefaultValue.integer(Integer.parseInt(value));
            case SAFELONG:
                return DefaultValue.safelong(SafeLong.valueOf(value));
            case DOUBLE:
                return DefaultValue.double_(Double.parseDouble(value));
            case BOOLEAN:
                return DefaultValue.boolean_(
                        value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("1"));
            default:
                throw new ConjureIllegalArgumentException(
                        String.format("Fields of type '%s' do not support default values", type.name()));
        }
    }

    @Override
    public DefaultValue visitLocalReference(LocalReferenceType type) {
        return parseEnumValue(type.type(), typeResolver.extractDefinition(type));
    }

    @Override
    public DefaultValue visitForeignReference(ForeignReferenceType type) {
        return parseEnumValue(type.type(), typeResolver.extractDefinition(type));
    }

    @Override
    public DefaultValue visitSet(SetType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'set' do not support default values");
    }

    @Override
    public DefaultValue visitBinary(BinaryType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'binary' do not support default values");
    }

    @Override
    public DefaultValue visitDateTime(DateTimeType _type) {
        throw new ConjureIllegalArgumentException("Fields of type 'datetime' do not support default values");
    }

    private DefaultValue parseEnumValue(TypeName typeName, Optional<BaseObjectTypeDefinition> typeDefinition) {
        return typeDefinition
                .flatMap(maybeDef -> maybeDef.visit(DEFINITION_EXTRACTOR)
                        .map(def -> def.fold(aliasDef -> aliasDef.alias().visit(this), enumDef -> {
                            Set<String> legalValues = enumDef.values().stream()
                                    .map(EnumValueDefinition::value)
                                    .collect(Collectors.toUnmodifiableSet());

                            if (!legalValues.contains(value)) {
                                throw new ConjureIllegalArgumentException(String.format(
                                        "'%s' is not a legal default value for type '%s'", value, typeName.name()));
                            }
                            return DefaultValue.enum_(value);
                        })))
                .orElseThrow(() -> new ConjureIllegalArgumentException(String.format(
                        "Only fields of primitive or enum type may have default values, not '%s'", typeName.name())));
    }

    private static final class TypeDefinitionExtractor
            implements TypeDefinitionVisitor<Optional<Either<AliasTypeDefinition, EnumTypeDefinition>>> {

        private TypeDefinitionExtractor() {}

        @Override
        public Optional<Either<AliasTypeDefinition, EnumTypeDefinition>> visit(AliasTypeDefinition def) {
            return Optional.of(Either.left(def));
        }

        @Override
        public Optional<Either<AliasTypeDefinition, EnumTypeDefinition>> visit(EnumTypeDefinition def) {
            return Optional.of(Either.right(def));
        }

        @Override
        public Optional<Either<AliasTypeDefinition, EnumTypeDefinition>> visit(ObjectTypeDefinition _def) {
            return Optional.empty();
        }

        @Override
        public Optional<Either<AliasTypeDefinition, EnumTypeDefinition>> visit(UnionTypeDefinition _def) {
            return Optional.empty();
        }
    }
}

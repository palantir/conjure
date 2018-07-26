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

package com.palantir.conjure.parser.types.primitive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.reference.LocalReferenceType;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum PrimitiveType implements LocalReferenceType {
    STRING(TypeName.of("string")),
    INTEGER(TypeName.of("integer")),
    DOUBLE(TypeName.of("double")),
    BOOLEAN(TypeName.of("boolean")),
    SAFELONG(TypeName.of("safelong")),
    RID(TypeName.of("rid")),
    BEARERTOKEN(TypeName.of("bearertoken")),
    UUID(TypeName.of("uuid")),
    ANY(TypeName.of("any"));

    private static final Map<TypeName, PrimitiveType> types =
            EnumSet.allOf(PrimitiveType.class).stream().collect(Collectors.toMap(
                    PrimitiveType::type, t -> t));

    private static final Set<TypeName> reservedTypes =
            ImmutableSortedSet.copyOf(
                    Comparator.comparing(TypeName::name, String.CASE_INSENSITIVE_ORDER),
                    types.keySet());

    private final TypeName type;

    PrimitiveType(TypeName type) {
        this.type = type;
    }

    @Override
    public TypeName type() {
        return type;
    }

    @Override
    public <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitPrimitive(this);
    }

    @JsonCreator
    public static PrimitiveType fromString(String type) {
        return fromTypeName(TypeName.of(type))
                .orElseThrow(() -> new IllegalArgumentException("Unknown primitive type: " + type));
    }

    public static Optional<PrimitiveType> fromTypeName(TypeName type) {
        PrimitiveType candidate = types.get(type);
        Preconditions.checkArgument(candidate != null || !reservedTypes.contains(type),
                "Invalid use of a built-in identifier (please check case): %s", type);
        return Optional.ofNullable(candidate);
    }
}

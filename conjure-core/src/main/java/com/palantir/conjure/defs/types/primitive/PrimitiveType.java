/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.primitive;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.LocalReferenceType;
import com.palantir.conjure.parser.ConjureMetrics;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum PrimitiveType implements LocalReferenceType {

    STRING(TypeName.of("string", ConjurePackage.PRIMITIVE)),
    INTEGER(TypeName.of("integer", ConjurePackage.PRIMITIVE)),
    DOUBLE(TypeName.of("double", ConjurePackage.PRIMITIVE)),
    BOOLEAN(TypeName.of("boolean", ConjurePackage.PRIMITIVE)),
    SAFELONG(TypeName.of("safelong", ConjurePackage.PRIMITIVE)),
    RID(TypeName.of("rid", ConjurePackage.PRIMITIVE)),
    BEARERTOKEN(TypeName.of("bearertoken", ConjurePackage.PRIMITIVE));

    private static final Map<TypeName, PrimitiveType> types =
            EnumSet.allOf(PrimitiveType.class).stream().collect(Collectors.toMap(PrimitiveType::type, t -> t));

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

    public static Optional<PrimitiveType> fromTypeName(TypeName type) {
        PrimitiveType candidate = types.get(type);
        Preconditions.checkArgument(candidate != null || !reservedTypes.contains(type),
                "Invalid use of a built-in identifier (please check case): %s", type);

        if (candidate != null) {
            ConjureMetrics.incrementCounter(PrimitiveType.class, candidate.type().name());
        }

        return Optional.ofNullable(candidate);
    }

    public static PrimitiveType parseFrom(com.palantir.conjure.parser.types.primitive.PrimitiveType primitiveType) {
        return PrimitiveType.valueOf(primitiveType.name());
    }
}

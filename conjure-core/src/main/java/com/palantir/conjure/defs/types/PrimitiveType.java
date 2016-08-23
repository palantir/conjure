/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum PrimitiveType implements ReferenceType {
    STRING("string"),
    INTEGER("integer"),
    DOUBLE("double"),
    BOOLEAN("boolean");

    private static final Map<String, PrimitiveType> types =
            EnumSet.allOf(PrimitiveType.class).stream().collect(Collectors.toMap(PrimitiveType::type, t -> t));

    private static final Set<String> reservedTypes =
            ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, types.keySet());

    private final String type;

    PrimitiveType(String type) {
        this.type = type;
    }

    @Override
    public String type() {
        return type;
    }

    @JsonCreator
    public static PrimitiveType fromString(String type) {
        return fromTypeString(type).orElseThrow(() -> new IllegalArgumentException("Unknown primitive type: " + type));
    }

    public static Optional<PrimitiveType> fromTypeString(String type) {
        PrimitiveType candidate = types.get(type);
        Preconditions.checkArgument(candidate != null || !reservedTypes.contains(type),
                "Invalid use of a built-in identifier (please check case): %s", type);
        return Optional.ofNullable(candidate);
    }
}

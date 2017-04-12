/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ObjectsDefinition;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;
import org.immutables.value.Value;

/**
 * Represents the name of a conjure {@link ObjectsDefinition#objects() object}.
 * TODO
 */
@Value.Immutable
@ConjureImmutablesStyle
@Immutable
public abstract class TypeName {

    private static final Pattern CUSTOM_TYPE_PATTERN = Pattern.compile("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$");
    @VisibleForTesting
    static final Set<String> PRIMITIVE_TYPES =
            ImmutableSet.of("unknown", "string", "integer", "double", "boolean");


    public static final TypeName UNKNOWN = TypeName.of("unknown");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                CUSTOM_TYPE_PATTERN.matcher(name()).matches() || PRIMITIVE_TYPES.contains(name()),
                "TypeNames must be a primitive type %s or match pattern %s: %s",
                PRIMITIVE_TYPES, CUSTOM_TYPE_PATTERN, name());
    }

    @JsonCreator
    public static TypeName of(String name) {
        return ImmutableTypeName.builder().name(name).build();
    }
}

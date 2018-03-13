/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.names;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ObjectsDefinition;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of a conjure {@link ObjectsDefinition#types() type} by a name and its conjure package.
 */
@com.google.errorprone.annotations.Immutable
@SuppressWarnings("Immutable")
@Value.Immutable
@ConjureImmutablesStyle
public abstract class TypeName {

    private static final Pattern CUSTOM_TYPE_PATTERN = Pattern.compile("^[A-Z][a-z0-9]+([A-Z][a-z0-9]+)*$");
    static final ImmutableSet<String> PRIMITIVE_TYPES =
            ImmutableSet.of(
                    "unknown", "string", "integer", "double", "boolean", "safelong", "rid", "bearertoken", "uuid");

    public abstract String name();

    public abstract ConjurePackage conjurePackage();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                CUSTOM_TYPE_PATTERN.matcher(name()).matches() || PRIMITIVE_TYPES.contains(name()),
                "TypeNames must be a primitive type %s or match pattern %s: %s",
                PRIMITIVE_TYPES, CUSTOM_TYPE_PATTERN, name());
    }

    public static TypeName of(String name, ConjurePackage conjurePackage) {
        return ImmutableTypeName.builder().name(name).conjurePackage(conjurePackage).build();
    }
}

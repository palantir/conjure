/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.names;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.TypesDefinition;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of a namespace for conjure imports, cf. {@link TypesDefinition#conjureImports()}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class Namespace {

    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^[a-z][a-z]+([A-Z][a-z]+)*$");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                NAMESPACE_PATTERN.matcher(name()).matches(),
                "Namespaces must match pattern %s: %s", NAMESPACE_PATTERN, name());
    }

    @JsonCreator
    public static Namespace of(String name) {
        return ImmutableNamespace.builder().name(name).build();
    }
}

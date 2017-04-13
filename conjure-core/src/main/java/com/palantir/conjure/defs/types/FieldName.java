/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Preconditions;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.regex.Pattern;
import org.immutables.value.Value;

/**
 * Represents the name of an {@link ObjectTypeDefinition#fields() field} of an {@link ObjectTypeDefinition}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public abstract class FieldName {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9]+([A-Z][a-z0-9]+)*$");
    private static final Pattern KEBAB_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9]+(-[a-z][a-z0-9]+)*$");
    private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("^[a-z][a-z0-9]+(_[a-z][a-z0-9]+)*$");

    @JsonValue
    public abstract String name();

    @Value.Check
    protected final void check() {
        Preconditions.checkArgument(
                CAMEL_CASE_PATTERN.matcher(name()).matches()
                        || KEBAB_CASE_PATTERN.matcher(name()).matches()
                        || SNAKE_CASE_PATTERN.matcher(name()).matches(),
                "FieldNames must be in lowerCamelCase (%s), kebab-case (%s), or snake_case (%s): %s",
                CAMEL_CASE_PATTERN, KEBAB_CASE_PATTERN, SNAKE_CASE_PATTERN, name());
    }

    @JsonCreator
    public static FieldName of(String name) {
        return ImmutableFieldName.builder().name(name).build();
    }
}

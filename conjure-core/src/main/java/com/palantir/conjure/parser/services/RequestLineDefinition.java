/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.ConjureMetrics;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface RequestLineDefinition {

    String method();

    PathDefinition path();

    static RequestLineDefinition of(String method, PathDefinition path) {
        return ImmutableRequestLineDefinition.builder().method(method).path(path).build();
    }

    @JsonCreator
    static RequestLineDefinition valueOf(String oneline) {
        String[] parts = oneline.split(" ", 2);
        checkArgument(parts.length == 2,
                "Request line must be of the form: [METHOD] [PATH], instead was '%s'",
                oneline);
        ConjureMetrics.incrementCounter(RequestLineDefinition.class, "method", parts[0]);
        return of(parts[0], PathDefinition.of(parts[1]));
    }

    @JsonValue
    default String asString() {
        return String.format("%s %s", method(), path());
    }
}

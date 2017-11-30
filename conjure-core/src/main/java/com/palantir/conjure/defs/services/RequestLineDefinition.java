/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Set;
import java.util.stream.Collectors;
import org.glassfish.jersey.uri.UriTemplate;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface RequestLineDefinition {

    String method();

    PathDefinition path();

    @Value.Derived
    default Set<ParameterName> pathArgs() {
        UriTemplate uriTemplate = new UriTemplate(path().path().toString());
        return uriTemplate.getTemplateVariables()
                .stream()
                .map(ParameterName::of)
                .collect(Collectors.toSet());
    }

    static RequestLineDefinition of(String method, PathDefinition path) {
        return ImmutableRequestLineDefinition.builder().method(method).path(path).build();
    }

    @JsonCreator
    static RequestLineDefinition valueOf(String oneline) {
        String[] parts = oneline.split(" ", 2);
        checkArgument(parts.length == 2,
                "Request line must be of the form: [METHOD] [PATH], instead was '%s'",
                oneline);
        return of(parts[0], PathDefinition.of(parts[1]));
    }

    @JsonValue
    default String asString() {
        return String.format("%s %s", method(), path());
    }
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

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

}

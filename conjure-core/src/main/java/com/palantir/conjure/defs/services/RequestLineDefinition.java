/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.services.RequestLineDefinition.RequestLineDefinitionDeserializer;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.glassfish.jersey.uri.UriTemplate;
import org.immutables.value.Value;

@JsonDeserialize(using = RequestLineDefinitionDeserializer.class)
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

    // solve Jackson sad-times for multiple parser
    class RequestLineDefinitionDeserializer extends JsonDeserializer<RequestLineDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public RequestLineDefinition deserialize(JsonParser parser, DeserializationContext context) throws IOException {

            String candidate = parser.getValueAsString();
            if (candidate != null) {
                String[] parts = candidate.split(" ", 2);
                checkArgument(parts.length == 2,
                        "Request line must be of the form: [METHOD] [PATH], instead was '%s'",
                        candidate);
                return of(parts[0], PathDefinition.of(parts[1]));
            }

            return ImmutableRequestLineDefinition.fromJson(
                    parser.readValueAs(ImmutableRequestLineDefinition.Json.class));
        }
    }

}

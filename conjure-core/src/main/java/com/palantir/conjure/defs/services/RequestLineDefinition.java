/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    @JsonProperty("method")
    String method();

    @JsonProperty("path")
    String path();

    @Value.Derived
    default Set<String> pathArgs() {
        UriTemplate uriTemplate = new UriTemplate(path());
        return uriTemplate.getTemplateVariables().stream().collect(Collectors.toSet());
    }

    static RequestLineDefinition of(String method, String path) {
        return ImmutableRequestLineDefinition.builder().method(method).path(path).build();
    }

    // solve Jackson sad-times for multiple parser
    class RequestLineDefinitionDeserializer extends JsonDeserializer<RequestLineDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public RequestLineDefinition deserialize(JsonParser parser, DeserializationContext context)
                throws IOException, JsonProcessingException {

            String candidate = parser.getValueAsString();
            if (candidate != null) {
                String[] parts = candidate.split(" ", 2);
                checkArgument(parts.length == 2, "Request line must be of the form: [METHOD] [PATH]");
                return of(parts[0], parts[1]);
            }

            return ImmutableRequestLineDefinition.fromJson(
                    parser.readValueAs(ImmutableRequestLineDefinition.Json.class));
        }
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.services.ArgumentDefinition.ArgumentDefinitionDeserializer;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(using = ArgumentDefinitionDeserializer.class)
@Value.Immutable
public interface ArgumentDefinition {

    ConjureType type();

    Optional<String> docs();

    static ArgumentDefinition of(ConjureType type) {
        return ImmutableArgumentDefinition.builder().type(type).build();
    }

    static ArgumentDefinition of(ConjureType type, String docs) {
        return ImmutableArgumentDefinition.builder().type(type).docs(docs).build();
    }

    // solve Jackson sad-times for multiple parser
    class ArgumentDefinitionDeserializer extends JsonDeserializer<ArgumentDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public ArgumentDefinition deserialize(JsonParser parser, DeserializationContext context)
                throws IOException, JsonProcessingException {

            String candidate = parser.getValueAsString();
            if (candidate != null) {
                try {
                    return of(ConjureType.fromString(candidate));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            return ImmutableArgumentDefinition.fromJson(
                    parser.readValueAs(ImmutableArgumentDefinition.Json.class));
        }
    }


}

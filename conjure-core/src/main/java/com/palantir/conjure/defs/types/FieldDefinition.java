/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.FieldDefinition.FieldDefinitionDeserializer;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = FieldDefinitionDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface FieldDefinition {

    ConjureType type();

    Optional<String> docs();

    static FieldDefinition of(ConjureType type) {
        return ImmutableFieldDefinition.builder().type(type).build();
    }

    // solve Jackson sad-times for multiple parser
    class FieldDefinitionDeserializer extends JsonDeserializer<FieldDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public FieldDefinition deserialize(JsonParser parser, DeserializationContext context) throws IOException {

            String candidate = parser.getValueAsString();
            if (candidate != null) {
                try {
                    return of(ConjureType.fromString(candidate));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            return ImmutableFieldDefinition.fromJson(
                    parser.readValueAs(ImmutableFieldDefinition.Json.class));
        }
    }

}

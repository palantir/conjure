/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.EnumValueDefinition.EnumValueDeserializer;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = EnumValueDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EnumValueDefinition {

    String value();

    Optional<String> docs();

    static EnumValueDefinition.Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumValueDefinition.Builder {}

    class EnumValueDeserializer extends JsonDeserializer<EnumValueDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public EnumValueDefinition deserialize(JsonParser parser, DeserializationContext ctxt)
                throws IOException {
            String candidate = parser.getValueAsString();
            if (candidate != null) {
                return builder().value(candidate).build();
            }

            return ImmutableEnumValueDefinition.fromJson(
                    parser.readValueAs(ImmutableEnumValueDefinition.Json.class));
        }
    }

}

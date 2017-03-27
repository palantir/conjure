/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.UnionValueDefinition.UnionValueDeserializer;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = UnionValueDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface UnionValueDefinition {

    String value();

    Optional<String> docs();

    static UnionValueDefinition.Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableUnionValueDefinition.Builder {}

    class UnionValueDeserializer extends JsonDeserializer<UnionValueDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public UnionValueDefinition deserialize(JsonParser parser, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String candidate = parser.getValueAsString();
            if (candidate != null) {
                return builder().value(candidate).build();
            }

            return ImmutableUnionValueDefinition.fromJson(
                    parser.readValueAs(ImmutableUnionValueDefinition.Json.class));
        }
    }
}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.complex.UnionMemberTypeDefinition.UnionMemberTypeDeserializer;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = UnionMemberTypeDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface UnionMemberTypeDefinition {

    String type();

    Optional<String> docs();

    static UnionMemberTypeDefinition.Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableUnionMemberTypeDefinition.Builder {}

    class UnionMemberTypeDeserializer extends JsonDeserializer<UnionMemberTypeDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public UnionMemberTypeDefinition deserialize(JsonParser parser, DeserializationContext ctxt)
                throws IOException {
            String candidate = parser.getValueAsString();
            if (candidate != null) {
                return builder().type(candidate).build();
            }

            return ImmutableUnionMemberTypeDefinition.fromJson(
                    parser.readValueAs(ImmutableUnionMemberTypeDefinition.Json.class));
        }
    }
}

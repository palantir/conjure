/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.complex.UnionMemberTypeDefinition.UnionMemberTypeDeserializer;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = UnionMemberTypeDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface UnionMemberTypeDefinition {

    ConjureType type();

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
                try {
                    return builder().type(ConjureType.fromString(candidate)).build();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            return ImmutableUnionMemberTypeDefinition.fromJson(
                    parser.readValueAs(ImmutableUnionMemberTypeDefinition.Json.class));
        }
    }
}

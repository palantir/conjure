/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@JsonDeserialize(using = ArgumentDefinitionDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ArgumentDefinition {

    ConjureType type();

    Optional<String> docs();

    @JsonProperty("param-id")
    Optional<ParameterName> paramId();

    @JsonProperty("param-type")
    @Value.Default
    default ParamType paramType() {
        return ParamType.AUTO;
    }

    Set<ConjureType> markers();

    enum ParamType {
        /**
         * Choose PathParam when this argument appears in the http line, treat as body otherwise.
         */
        AUTO,
        /**
         * Treat as a PathParam.
         */
        PATH,
        /**
         * Treat as a QueryParam.
         */
        QUERY,
        /**
         * Treat as a HeaderParam.
         */
        HEADER,
        /**
         * Treat as the message body.
         */
        BODY;

        @JsonCreator
        public static ParamType fromString(String val) {
            return ParamType.valueOf(val.toUpperCase(Locale.ROOT));
        }
    }

    static ArgumentDefinition of(ConjureType type) {
        return builder().type(type).build();
    }

    static ArgumentDefinition of(ConjureType type, String docs) {
        return builder().type(type).docs(docs).build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableArgumentDefinition.Builder {}

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

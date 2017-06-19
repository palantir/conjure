/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Locale;
import org.immutables.value.Value;

@JsonDeserialize(as = AuthDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface AuthDefinition {

    String AUTH_HEADER = "Authorization";

    AuthDefinition.AuthType type();

    String id();

    enum AuthType {
        NONE,
        HEADER,
        COOKIE;

        @JsonCreator
        public static AuthType fromString(String value) {
            return AuthType.valueOf(value.toUpperCase(Locale.ROOT));
        }
    }

    static AuthDefinition none() {
        return ImmutableAuthDefinition.builder()
                .type(AuthType.NONE)
                .id("NONE")
                .build();
    }

    static AuthDefinition header() {
        return ImmutableAuthDefinition.builder()
                .type(AuthType.HEADER)
                .id(AUTH_HEADER)
                .build();
    }

    static AuthDefinition cookie(String id) {
        return ImmutableAuthDefinition.builder()
                .type(AuthType.COOKIE)
                .id(id)
                .build();
    }

    @JsonCreator
    static AuthDefinition fromString(String value) {
        String[] parts = value.split(":", 2);
        AuthType type = AuthType.fromString(parts[0]);
        String id;
        switch (type) {
            case HEADER:
                id = AUTH_HEADER;
                break;
            case NONE:
                id = "NONE";
                break;
            case COOKIE:
                checkArgument(parts.length == 2, "Cookie authorization type must include a cookie name");
                id = parts[1];
                break;
            default:
                throw new IllegalArgumentException("Unknown authorization type: " + type);
        }
        return ImmutableAuthDefinition.builder().type(type).id(id).build();
    }

    @JsonValue
    default String value() {
        return type().name().toLowerCase(Locale.ROOT) + ":" + id();
    }
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = AuthorizationDefinition.class)
@JsonSerialize(as = AuthorizationDefinition.class)
@Value.Immutable
public interface AuthorizationDefinition {

    String HEADER_AUTHORIZATION = "Authorization";

    AuthorizationDefinition.AuthorizationType type();

    String id();

    enum AuthorizationType {
        NONE,
        HEADER,
        COOKIE;

        @JsonCreator
        public static AuthorizationType fromString(String value) {
            return AuthorizationType.valueOf(value.toUpperCase());
        }
    }

    static AuthorizationDefinition none() {
        return ImmutableAuthorizationDefinition.builder()
                .type(AuthorizationType.NONE)
                .id("NONE")
                .build();
    }

    static AuthorizationDefinition header() {
        return ImmutableAuthorizationDefinition.builder()
                .type(AuthorizationType.HEADER)
                .id(HEADER_AUTHORIZATION)
                .build();
    }

    static AuthorizationDefinition cookie(String id) {
        return ImmutableAuthorizationDefinition.builder()
                .type(AuthorizationType.COOKIE)
                .id(id)
                .build();
    }

    @JsonCreator
    static AuthorizationDefinition fromString(String value) {
        String[] parts = value.split(":", 2);
        AuthorizationType type = AuthorizationType.fromString(parts[0]);
        String id;
        switch (type) {
            case HEADER:
                id = HEADER_AUTHORIZATION;
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
        return ImmutableAuthorizationDefinition.builder().type(type).id(id).build();
    }

    @JsonValue
    default String value() {
        return type().name().toLowerCase() + ":" + id();
    }
}

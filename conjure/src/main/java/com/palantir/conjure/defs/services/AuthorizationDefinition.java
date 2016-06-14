/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(as = AuthorizationDefinition.class)
@Value.Immutable
public abstract class AuthorizationDefinition {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    public abstract AuthorizationDefinition.AuthorizationType type();

    public abstract String id();

    public enum AuthorizationType {
        NONE,
        HEADER,
        COOKIE;

        @JsonCreator
        public static AuthorizationType fromString(String value) {
            return AuthorizationType.valueOf(value.toUpperCase());
        }
    }

    public static AuthorizationDefinition none() {
        return ImmutableAuthorizationDefinition.builder()
                .type(AuthorizationType.NONE)
                .id("NONE")
                .build();
    }

    public static AuthorizationDefinition header() {
        return ImmutableAuthorizationDefinition.builder()
                .type(AuthorizationType.HEADER)
                .id(HEADER_AUTHORIZATION)
                .build();
    }

    public static AuthorizationDefinition cookie(String id) {
        return ImmutableAuthorizationDefinition.builder()
                .type(AuthorizationType.COOKIE)
                .id(id)
                .build();
    }

    @JsonCreator
    public static AuthorizationDefinition fromString(String value) {
        String[] parts = value.split(":", 2);
        AuthorizationType type = AuthorizationType.fromString(parts[0]);
        String id = getIdForType(parts, type);
        return ImmutableAuthorizationDefinition.builder().type(type).id(id).build();
    }

    private static String getIdForType(String[] parts, AuthorizationType type) {
        switch (type) {
            case HEADER:
                return HEADER_AUTHORIZATION;
            case NONE:
                return "NONE";
            case COOKIE:
                checkArgument(parts.length == 2, "Cookie authorization type must include a cookie name");
                return parts[1];
            default:
                throw new IllegalArgumentException("Unknown authorization type: " + type);
        }
    }

    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString() {
        return type().name().toLowerCase() + ":" + id();
    }
}

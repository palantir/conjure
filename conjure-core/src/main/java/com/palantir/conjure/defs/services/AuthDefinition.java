/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface AuthDefinition {

    AuthDefinition.AuthType type();

    String id();

    enum AuthType {
        NONE,
        HEADER,
        COOKIE
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
                .id("Authorization")
                .build();
    }

    static AuthDefinition parseFrom(com.palantir.conjure.parser.services.AuthDefinition auth) {
        return ImmutableAuthDefinition.builder()
                .type(AuthType.valueOf(auth.type().name()))
                .id(auth.id())
                .build();
    }
}

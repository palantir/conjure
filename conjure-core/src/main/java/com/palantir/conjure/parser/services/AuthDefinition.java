/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.parser.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.logsafe.Preconditions;
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
        return ImmutableAuthDefinition.builder().type(AuthType.NONE).id("NONE").build();
    }

    static AuthDefinition header() {
        return ImmutableAuthDefinition.builder()
                .type(AuthType.HEADER)
                .id(AUTH_HEADER)
                .build();
    }

    static AuthDefinition cookie(String id) {
        return ImmutableAuthDefinition.builder().type(AuthType.COOKIE).id(id).build();
    }

    @JsonCreator
    static AuthDefinition fromString(String value) {
        String[] parts = value.split(":", 2);
        AuthType type = AuthType.fromString(parts[0]);
        String id;
        switch (type) {
            case HEADER -> id = AUTH_HEADER;
            case NONE -> id = "NONE";
            case COOKIE -> {
                Preconditions.checkArgument(parts.length == 2, "Cookie authorization type must include a cookie name");
                id = parts[1];
            }
            default -> throw new IllegalArgumentException("Unknown authorization type: " + type);
        }
        return ImmutableAuthDefinition.builder().type(type).id(id).build();
    }

    @JsonValue
    default String value() {
        return type().name().toLowerCase(Locale.ROOT) + ":" + id();
    }
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@JsonDeserialize(as = ImmutableEndpointDefinition.class)
@JsonSerialize(as = ImmutableEndpointDefinition.class)
@Value.Immutable
public interface EndpointDefinition {

    Pattern PATH_ARG = Pattern.compile("\\{([^\\}]+)\\}");

    String http();

    Optional<AuthorizationType> authorization();

    Optional<Map<String, ArgumentDefinition>> args();

    Optional<ConjureType> returns();

    Optional<String> docs();

    // TODO(melliot) verify args and request line match

    static Builder builder() {
        return new Builder();
    }

    enum AuthorizationType {
        HEADER;

        @JsonCreator
        public static AuthorizationType fromString(String val) {
            return AuthorizationType.valueOf(val.toUpperCase());
        }
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}

    @Value.Derived
    default String method() {
        return http().substring(0, http().indexOf(' '));
    }

    @Value.Derived
    default String path() {
        return http().substring(http().indexOf(' ') + 1);
    }

    @Value.Derived
    default Set<String> pathArgs() {
        Matcher matcher = PATH_ARG.matcher(http());
        ImmutableSet.Builder<String> set = ImmutableSet.builder();
        while (matcher.find()) {
            set.add(matcher.group(1));
        }
        return set.build();
    }

}

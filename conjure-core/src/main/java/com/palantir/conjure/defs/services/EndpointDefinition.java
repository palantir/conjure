/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BinaryType;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEndpointDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    Set<Class<? extends ConjureType>> ILLEGAL_ARG_TYPES = ImmutableSet.of(BinaryType.class);

    @JsonProperty("http")
    RequestLineDefinition http();

    @JsonProperty("auth")
    Optional<AuthDefinition> auth();

    @JsonProperty("args")
    Optional<Map<String, ArgumentDefinition>> args();

    @JsonProperty("returns")
    Optional<ConjureType> returns();

    @JsonProperty("docs")
    Optional<String> docs();

    @JsonProperty("deprecated")
    Optional<String> deprecated();

    @Value.Check
    default void check() {
        toStream(args())
                .map(Map::values)
                .flatMap(Collection::stream)
                .map(ArgumentDefinition::type)
                .forEach(type -> checkArgument(!isIllegal(type), "Endpoint cannot have argument with type '%s'", type));
    }

    static boolean isIllegal(ConjureType type) {
        return ILLEGAL_ARG_TYPES.stream()
                .filter(illegalClass -> illegalClass.isAssignableFrom(type.getClass()))
                .count() > 0;
    }

    static <T> Stream<T> toStream(Optional<T> val) {
        return val.map(Stream::of).orElse(Stream.empty());
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}

}

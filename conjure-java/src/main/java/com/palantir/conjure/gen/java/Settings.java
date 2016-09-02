/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.gen.java.types.TypeMapper.OptionalTypeStrategy;
import org.immutables.value.Value;

@Value.Immutable
public interface Settings {

    @Value.Default
    default OptionalTypeStrategy optionalTypeStrategy() {
        return OptionalTypeStrategy.Java8;
    }

    @Value.Default
    default boolean ignoreUnknownProperties() {
        return false;
    }

    @Value.Default
    default boolean supportUnknownEnumValues() {
        return true;
    }

    static Builder builder() {
        return new Builder();
    }

    static Settings standard() {
        return new Builder().build();
    }

    class Builder extends ImmutableSettings.Builder {}

}

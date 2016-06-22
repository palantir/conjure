/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.gen.java.TypeMapper.OptionalTypeStrategy;
import org.immutables.value.Value;

@Value.Immutable
public interface Settings {

    @Value.Parameter
    OptionalTypeStrategy optionalTypeStrategy();

    static Settings of(OptionalTypeStrategy strategy) {
        return ImmutableSettings.of(strategy);
    }

}

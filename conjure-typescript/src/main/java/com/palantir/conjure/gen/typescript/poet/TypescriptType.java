/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptType extends Emittable {
    String name();

    static ImmutableTypescriptType.Builder builder() {
        return ImmutableTypescriptType.builder();
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name());
    }
}

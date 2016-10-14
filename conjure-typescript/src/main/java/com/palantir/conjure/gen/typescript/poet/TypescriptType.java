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

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name());
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptType.Builder {}
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptTypeGuardType extends TypescriptType {
    String variableName();
    TypescriptType predicateType();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(String.format("%s is ", variableName()));
        predicateType().emit(writer);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptTypeGuardType.Builder {}
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptTypeSignature extends Comparable<TypescriptTypeSignature>, Emittable {

    @Value.Default
    default Boolean isOptional() {
        return false;
    }
    String name();
    TypescriptType typescriptType();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name());
        if (isOptional()) {
            writer.write("?");
        }
        writer.write(": ");
        typescriptType().emit(writer);
    }

    @Override
    default int compareTo(TypescriptTypeSignature other) {
        if (this.isOptional().equals(other.isOptional())) {
            return this.name().compareTo(other.name());
        } else {
            return this.isOptional() ? 1 : -1;
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptTypeSignature.Builder {}
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptFunction extends Emittable, Exportable {

    TypescriptFunctionBody functionBody();
    TypescriptFunctionSignature functionSignature();

    @Value.Default
    default boolean isMethod() {
        return true;
    }

    @Value.Default
    default boolean export() {
        return false;
    }

    @Override
    @Value.Derived
    default Optional<String> exportName() {
        return export() ? Optional.of(functionSignature().name()) : Optional.empty();
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        if (export()) {
            writer.write("export ");
        }
        if (!isMethod()) {
            writer.writeIndented("function ");
        } else {
            writer.writeIndented("public ");
        }
        functionSignature().emit(writer);
        writer.write(" ");
        functionBody().emit(writer);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptFunction.Builder {}
}

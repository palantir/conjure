/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptFunctionSignature extends Comparable<TypescriptFunctionSignature>, Emittable {

    String name();
    List<TypescriptTypeSignature> parameters();
    Optional<TypescriptType> returnType();

    static ImmutableTypescriptFunctionSignature.Builder builder() {
        return ImmutableTypescriptFunctionSignature.builder();
    }

    @Override
    default int compareTo(TypescriptFunctionSignature other) {
        return this.name().compareTo(other.name());
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name());
        writer.write("(");
        if (parameters().size() > 0) {
            writer.writeLine();
            writer.increaseIndent();
            writer.writeIndented();
            parameters().get(0).emit(writer);
            parameters().subList(1, parameters().size()).forEach(parameter -> {
                writer.writeLine(",");
                writer.writeIndented();
                parameter.emit(writer);
            });
            writer.writeLine();
            writer.decreaseIndent();
            writer.writeIndented();
        }
        writer.write(")");
        if (returnType().isPresent()) {
            writer.write(": ");
            returnType().get().emit(writer);
        }
    }
}

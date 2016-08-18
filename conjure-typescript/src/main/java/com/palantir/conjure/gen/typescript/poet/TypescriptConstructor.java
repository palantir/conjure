/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptConstructor extends Emittable {

    TypescriptFunctionBody functionBody();
    List<TypescriptTypeSignature> parameters();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented();
        TypescriptFunctionSignature signature = TypescriptFunctionSignature.builder().name(
                "constructor").addAllParameters(parameters()).build();
        writer.emit(signature);
        writer.write(" ");
        writer.emit(functionBody());
    }

    static ImmutableTypescriptConstructor.Builder builder() {
        return ImmutableTypescriptConstructor.builder();
    }
}


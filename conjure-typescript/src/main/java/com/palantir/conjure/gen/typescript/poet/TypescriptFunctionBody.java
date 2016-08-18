/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptFunctionBody extends Emittable {
    List<TypescriptStatement> statements();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeLine("{");
        writer.increaseIndent();
        statements().forEach(statement -> statement.emit(writer));
        writer.decreaseIndent();
        writer.writeIndentedLine("}");
    }

    static ImmutableTypescriptFunctionBody.Builder builder() {
        return ImmutableTypescriptFunctionBody.builder();
    }
}

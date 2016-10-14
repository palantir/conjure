/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ReturnStatement extends TypescriptStatement {
    TypescriptExpression expression();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented("return ");
        writer.emit(expression());
        writer.writeLine(";");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableReturnStatement.Builder {}
}

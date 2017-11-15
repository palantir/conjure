/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ReturnStatement extends TypescriptStatement {
    @Value.Parameter
    TypescriptExpression expression();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented("return ");
        writer.emit(expression());
        writer.writeLine(";");
    }

    static ReturnStatement of(TypescriptExpression expression) {
        return ImmutableReturnStatement.of(expression);
    }
}

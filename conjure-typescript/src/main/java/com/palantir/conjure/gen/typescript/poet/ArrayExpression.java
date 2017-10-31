/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ArrayExpression extends TypescriptExpression {
    List<TypescriptExpression> expressions();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeLine("[");
        writer.increaseIndent();
        expressions().forEach(expression -> writer.writeIndented().emit(expression).writeLine(","));
        writer.decreaseIndent();
        writer.writeIndented("]");
    }

    static ArrayExpression of(TypescriptExpression... expressions) {
        return ImmutableArrayExpression.builder().addExpressions(expressions).build();
    }

    static ArrayExpression of(List<TypescriptExpression> expressions) {
        return ImmutableArrayExpression.builder().addAllExpressions(expressions).build();
    }
}

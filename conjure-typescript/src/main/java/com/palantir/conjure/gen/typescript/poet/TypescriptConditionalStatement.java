/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptConditionalStatement extends TypescriptStatement {

    TypescriptExpression conditionalExpression();
    Emittable equalityBody();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented("if (")
                .emit(conditionalExpression())
                .writeLine(") {")
                .increaseIndent()
                .emit(equalityBody())
                .decreaseIndent()
                .writeIndentedLine("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptConditionalStatement.Builder {}

}

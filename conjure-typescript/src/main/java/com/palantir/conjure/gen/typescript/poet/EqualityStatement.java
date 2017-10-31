/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface EqualityStatement extends TypescriptStatement {

    TypescriptEqualityStatement typescriptEqualityStatement();

    Emittable equalityBody();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented("if (");
        writer.emit(typescriptEqualityStatement());
        writer.write(")");
        writer.writeLine(" {");
        writer.emitIndented(equalityBody());
        writer.writeIndentedLine("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEqualityStatement.Builder {}

}

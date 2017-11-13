/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Comparator;
import java.util.Map;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ObjectExpression extends TypescriptExpression {
    Map<StringExpression, TypescriptExpression> keyValues();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeLine("{");
        writer.increaseIndent();
        keyValues().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().content()))
                .forEach(e -> writer.writeIndented(e.getKey().emitToString() + ": ").emit(e.getValue()).writeLine(","));
        writer.decreaseIndent();
        writer.writeIndented("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectExpression.Builder {}
}

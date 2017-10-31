/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Map;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface JsonExpression extends TypescriptExpression {
    Map<String, TypescriptExpression> keyValues();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeLine("{");
        writer.increaseIndent();
        keyValues().entrySet().stream().sorted((first, second) -> first.getKey().compareTo(second.getKey())).forEach(
                e -> writer.writeIndented(e.getKey() + ": ").emit(e.getValue()).writeLine(","));
        writer.decreaseIndent();
        writer.writeIndented("}");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableJsonExpression.Builder {}
}

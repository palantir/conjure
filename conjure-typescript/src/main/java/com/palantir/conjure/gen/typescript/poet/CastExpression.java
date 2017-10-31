/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface CastExpression extends TypescriptExpression {
    TypescriptExpression expression();
    String type();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.emit(expression()).write(" as ").write(type());
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableCastExpression.Builder {}
}

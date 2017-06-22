/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptEqualityClause extends TypescriptExpression {
    TypescriptExpression lhs();
    TypescriptExpression rhs();
    @Value.Default
    default boolean useTripleEquals() {
        return true;
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write("(");
        lhs().emit(writer);
        writer.write(useTripleEquals() ? " === " : " == ");
        rhs().emit(writer);
        writer.write(")");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptEqualityClause.Builder {}

}

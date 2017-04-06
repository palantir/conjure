/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptEqualityBinaryOperatorStatement extends TypescriptEqualityStatement {

    TypescriptEqualityStatement leftHandStatement();
    TypescriptEqualityStatement rightHandStatement();
    String operator();

    static TypescriptEqualityBinaryOperatorStatement or(TypescriptEqualityStatement lhs,
                                                        TypescriptEqualityStatement rhs) {
        return ImmutableTypescriptEqualityBinaryOperatorStatement.builder()
                .leftHandStatement(lhs)
                .rightHandStatement(rhs)
                .operator("||")
                .build();
    }

    static TypescriptEqualityBinaryOperatorStatement and(TypescriptEqualityStatement lhs,
                                                         TypescriptEqualityStatement rhs) {
        return ImmutableTypescriptEqualityBinaryOperatorStatement.builder()
                .leftHandStatement(lhs)
                .rightHandStatement(rhs)
                .operator("&&")
                .build();
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write("(");
        writer.emit(leftHandStatement());
        writer.write(" " + operator() + " ");
        writer.emit(rightHandStatement());
        writer.write(")");
    }

}

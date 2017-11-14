/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

/**
 * Typescript allows string literals to appear in type signatures.
 *
 * https://www.typescriptlang.org/docs/handbook/advanced-types.html#string-literal-types.
 *
 * The only difference from the StringExpression class is the {@link TypescriptType} marker interface,
 * which allows us to build up more complicated type signatures using this class.
 */
@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptStringLiteralType extends TypescriptType {

    @Value.Parameter
    StringExpression literal();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        literal().emit(writer);
    }

    static TypescriptStringLiteralType of(StringExpression literal) {
        return ImmutableTypescriptStringLiteralType.of(literal);
    }
}

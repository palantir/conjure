/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface FunctionCallExpression extends TypescriptExpression {
    List<TypescriptExpression> arguments();
    String name();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(name() + "(");
        writer.emitJoin(arguments(), ", ");
        writer.write(")");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableFunctionCallExpression.Builder {}
}

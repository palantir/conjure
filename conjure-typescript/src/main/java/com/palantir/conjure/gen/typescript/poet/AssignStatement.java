/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Optional;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface AssignStatement extends TypescriptStatement {
    String lhs();
    Optional<TypescriptExpression> rhs();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented(lhs());
        if (rhs().isPresent()) {
            writer.write(" = ");
            writer.emit(rhs().get());
        }
        writer.writeLine(";");
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableAssignStatement.Builder {}
}

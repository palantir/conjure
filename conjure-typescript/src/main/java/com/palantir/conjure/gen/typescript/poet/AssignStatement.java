/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Optional;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface AssignStatement extends TypescriptStatement {
    String lhs();
    @Value.Default
    default Optional<String> rhs() {
        return Optional.absent();
    }

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndented(lhs());
        if (rhs().isPresent()) {
            writer.write(" = ");
            writer.write(rhs().get());
        }
        writer.writeLine(";");
    }

    static ImmutableAssignStatement.Builder builder() {
        return ImmutableAssignStatement.builder();
    }
}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface TypescriptEqualityClause extends TypescriptEqualityStatement {

    String clause();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.write(clause());
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypescriptEqualityClause.Builder {}

}

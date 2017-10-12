/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface StarImportStatement extends ImportStatement {
    String variableName();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndentedLine(
                "import * as " + variableName() + " from \"" + filepathToImport() + "\";");
    }

    @Override
    default <T> T accept(ImportStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableStarImportStatement.Builder {}
}

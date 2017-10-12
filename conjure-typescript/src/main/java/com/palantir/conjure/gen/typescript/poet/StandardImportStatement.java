/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Joiner;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface StandardImportStatement extends ImportStatement {
    List<String> namesToImport();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndentedLine(
                "import { " + Joiner.on(", ").join(namesToImport()) + " } from \"" + filepathToImport() + "\";");
    }

    @Override
    default <T> T accept(ImportStatementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableStandardImportStatement.Builder {}
}

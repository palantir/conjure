/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Joiner;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ImportStatement extends Comparable<ImportStatement>, Emittable {
    List<String> namesToImport();
    String filepathToImport();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        writer.writeIndentedLine(
                "import { " + Joiner.on(", ").join(namesToImport()) + " } from \"" + filepathToImport() + "\";");
    }

    static ImmutableImportStatement.Builder builder() {
        return ImmutableImportStatement.builder();
    }

    @Override
    default int compareTo(ImportStatement other) {
        int pathCompare = this.filepathToImport().compareTo(other.filepathToImport());
        if (pathCompare == 0) {
            for (int nameIndex = 0; nameIndex < Math.min(this.namesToImport().size(),
                    other.namesToImport().size()); nameIndex++) {
                int nameCompare = this.namesToImport().get(nameIndex).compareTo(other.namesToImport().get(nameIndex));
                if (nameCompare != 0) {
                    return nameCompare;
                }
            }
            return this.namesToImport().size() - other.namesToImport().size();
        } else {
            return pathCompare;
        }
    }
}

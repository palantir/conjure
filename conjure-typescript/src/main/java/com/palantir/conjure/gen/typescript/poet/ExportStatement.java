/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import com.google.common.base.Joiner;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@ConjureImmutablesStyle
@Value.Immutable
public interface ExportStatement extends Comparable<ExportStatement>, Emittable {
    Set<String> namesToExport();
    String filepathToExport();

    @Override
    default void emit(TypescriptPoetWriter writer) {
        List<String> sortedNames = namesToExport().stream().sorted().collect(Collectors.toList());
        writer.writeIndentedLine(
                "export { " + Joiner.on(", ").join(sortedNames) + " } from \"" + filepathToExport() + "\";");
    }

    @Override
    default int compareTo(ExportStatement other) {
        int pathCompare = this.filepathToExport().compareTo(other.filepathToExport());
        if (pathCompare != 0) {
            return pathCompare;
        }

        List<String> sortedNames = namesToExport().stream().sorted().collect(Collectors.toList());
        List<String> otherSortedNames = other.namesToExport().stream().sorted().collect(Collectors.toList());
        for (int nameIndex = 0; nameIndex < Math.min(sortedNames.size(), otherSortedNames.size()); nameIndex++) {
            int nameCompare = sortedNames.get(nameIndex).compareTo(otherSortedNames.get(nameIndex));
            if (nameCompare != 0) {
                return nameCompare;
            }
        }
        return this.namesToExport().size() - other.namesToExport().size();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableExportStatement.Builder {}
}

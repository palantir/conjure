/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An implementation of {@link DuplicateExportHandler} that drops duplicates.
 */
public final class DropDuplicateExportHandler implements DuplicateExportHandler {

    @Override
    public Set<ExportStatement> handleDuplicates(Set<ExportStatement> exports) {
        Map<String, Long> occurrences = exports.stream()
                .flatMap(export -> export.namesToExport().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Set<String> duplicateExportNames = occurrences.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return exports.stream()
                .map(export -> removeDuplicates(export, duplicateExportNames))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Optional<ExportStatement> removeDuplicates(ExportStatement original, Set<String> duplicateExportNames) {
        Set<String> namesToExport = Sets.difference(Sets.newHashSet(original.namesToExport()), duplicateExportNames);
        return namesToExport.isEmpty() ? Optional.absent()
                : Optional.of(ExportStatement.builder().from(original).namesToExport(namesToExport).build());
    }

}

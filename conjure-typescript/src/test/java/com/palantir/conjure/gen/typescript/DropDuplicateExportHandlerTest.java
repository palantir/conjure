/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import java.util.Set;
import org.junit.Test;

public final class DropDuplicateExportHandlerTest {

    @Test
    public void testHandleDuplicates() {
        DuplicateExportHandler handler = new DropDuplicateExportHandler();
        Set<ExportStatement> inputSet = ImmutableSet.of(
                ExportStatement.builder().addNamesToExport("Foo").filepathToExport("file1").build(),
                ExportStatement.builder().addNamesToExport("Foo").filepathToExport("file2").build(),
                ExportStatement.builder().addNamesToExport("Bar").filepathToExport("file2").build());
        Set<ExportStatement> outputSet = handler.handleDuplicates(inputSet);
        assertThat(outputSet.size()).isEqualTo(1);
        assertThat(Iterables.getOnlyElement(outputSet)).isEqualTo(
                ExportStatement.builder().addNamesToExport("Bar").filepathToExport("file2").build());
    }
}

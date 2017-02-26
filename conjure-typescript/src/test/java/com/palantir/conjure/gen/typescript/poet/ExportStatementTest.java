/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import static org.mockito.Mockito.verify;

import java.io.PrintStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public final class ExportStatementTest {

    @Mock
    private PrintStream printStream;

    private TypescriptPoetWriter writer;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        writer = new TypescriptPoetWriter(printStream);
    }

    @Test
    public void testEmitSingleExport() {
        ExportStatement exportStatement = ExportStatement.builder()
                .addNamesToExport("Foo")
                .filepathToExport("filepath")
                .build();
        exportStatement.emit(writer);
        verify(printStream).print("export { Foo } from \"filepath\";");
    }

    @Test
    public void testEmitMultipleExports() {
        ExportStatement exportStatement = ExportStatement.builder()
                .addNamesToExport("Foo")
                .addNamesToExport("Bar")
                .filepathToExport("filepath")
                .build();
        exportStatement.emit(writer);
        verify(printStream).print("export { Bar, Foo } from \"filepath\";");
    }

    @Test
    public void testEmitDuplicateExports() {
        ExportStatement exportStatement = ExportStatement.builder()
                .addNamesToExport("Foo")
                .addNamesToExport("Foo")
                .filepathToExport("filepath")
                .build();
        exportStatement.emit(writer);
        verify(printStream).print("export { Foo } from \"filepath\";");
    }

}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.palantir.conjure.gen.typescript.poet.ImportStatement;
import com.palantir.conjure.gen.typescript.poet.StandardImportStatement;
import com.palantir.conjure.gen.typescript.poet.StarImportStatement;
import java.util.List;
import org.junit.Test;

public class ImportStatementsTests {

    @Test
    public void comparatorTest() {
        ImportStatement first = StandardImportStatement.builder()
                .filepathToImport("./a")
                .addNamesToImport("IGoo")
                .build();
        ImportStatement second = StarImportStatement.builder()
                .filepathToImport("./a")
                .variableName("_IFoo")
                .build();
        ImportStatement third = StarImportStatement.builder()
                .filepathToImport("./a")
                .variableName("_IGoo")
                .build();
        ImportStatement fourth = StandardImportStatement.builder()
                .filepathToImport("./b")
                .addNamesToImport("IFoo", "IBar")
                .build();
        ImportStatement fifth = StandardImportStatement.builder()
                .filepathToImport("./b")
                .addNamesToImport("IFoo", "IBar", "IBaz")
                .build();
        ImportStatement sixth = StandardImportStatement.builder()
                .filepathToImport("./b")
                .addNamesToImport("IGoo")
                .build();
        ImportStatement seventh = StarImportStatement.builder()
                .filepathToImport("./b")
                .variableName("_IFoo")
                .build();
        List<ImportStatement> expected = ImmutableList.of(
                first,
                second,
                third,
                fourth,
                fifth,
                sixth,
                seventh);
        List<ImportStatement> actual = ImmutableList.copyOf(ImmutableSortedSet.of(
                third,
                second,
                fifth,
                fourth,
                seventh,
                sixth,
                first));
        assertThat(actual).isEqualTo(expected);
    }
}

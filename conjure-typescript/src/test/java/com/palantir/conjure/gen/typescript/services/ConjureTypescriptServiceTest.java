/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public final class ConjureTypescriptServiceTest {

    @Test
    public void testTypescriptServiceGenerator_generate_testService() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/services/test-service.yml"));

        Set<TypescriptFile> files = new DefaultServiceGenerator().generate(def);

        for (TypescriptFile file : files) {
            assertThat(file.writeToString()).isEqualTo(CharStreams.toString(new InputStreamReader(
                    getClass().getResourceAsStream("/services/" + StringUtils.uncapitalize(file.name()) + ".ts.output"),
                    StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void testTypescriptServiceGenerator_generateExports_testService() {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/services/test-service.yml"));
        Set<ExportStatement> exports = new DefaultServiceGenerator().generateExports(def);

        assertThat(exports.size()).isEqualTo(2);
        assertThat(exports).containsAll(ImmutableSet.of(
                ExportStatement.builder()
                        .addNamesToExport("ITestService")
                        .filepathToExport("./foundry/catalog/api/testService")
                        .build(),
                ExportStatement.builder()
                        .addNamesToExport("TestService")
                        .filepathToExport("./foundry/catalog/api/testServiceImpl")
                        .build()));
    }

    @Test
    public void testTypescriptServiceGenerator_generateExports_testServiceDuplicate() {
        ConjureDefinition def = Conjure.parse(
                getClass().getResourceAsStream("/services/test-service-duplicates.yml"));
        Set<ExportStatement> exports = new DefaultServiceGenerator().generateExports(def);

        // duplicate names are fine at this level; they are expected to be handled by caller
        assertThat(exports.size()).isEqualTo(4);
        assertThat(exports).containsExactlyInAnyOrder(
                ExportStatement.builder()
                        .addNamesToExport("IDuplicateService")
                        .filepathToExport("./test/api/duplicateService")
                        .build(),
                ExportStatement.builder()
                        .addNamesToExport("DuplicateService")
                        .filepathToExport("./test/api/duplicateServiceImpl")
                        .build(),
                ExportStatement.builder()
                        .addNamesToExport("IMyDuplicateService")
                        .filepathToExport("./test/api/myDuplicateService")
                        .build(),
                ExportStatement.builder()
                        .addNamesToExport("MyDuplicateService")
                        .filepathToExport("./test/api/myDuplicateServiceImpl")
                        .build());
    }
}

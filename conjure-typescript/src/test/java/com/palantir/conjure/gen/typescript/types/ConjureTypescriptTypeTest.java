/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public final class ConjureTypescriptTypeTest {

    @Test
    public void testTypescriptTypeGenerator_generate_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-types.yml"));
        Set<TypescriptFile> files = new DefaultTypeGenerator().generate(def.types());
        for (TypescriptFile file : files) {
            assertThat(file.writeToString()).isEqualTo(CharStreams.toString(new InputStreamReader(
                    getClass().getResourceAsStream("/types/" + StringUtils.uncapitalize(file.name()) + ".ts"),
                    StandardCharsets.UTF_8)));
        }
    }

    @Test
    public void testTypescriptTypeGenerator_generateExports_allExamples() {
        ConjureDefinition def = Conjure.parse(new File("src/test/resources/example-types.yml"));
        Set<ExportStatement> exports = new DefaultTypeGenerator().generateExports(def.types());

        assertThat(exports).containsExactlyInAnyOrder(exportStatement("IStringExample", "stringExample"),
                exportStatement("IIntegerExample", "integerExample"),
                exportStatement("IDoubleExample", "doubleExample"),
                exportStatement("IOptionalExample", "optionalExample"),
                exportStatement("IListExample", "listExample"),
                exportStatement("ISetExample", "setExample"),
                exportStatement("IMapExample", "mapExample"),
                exportStatement("EnumExample", "enumExample"),
                exportStatement("IBooleanExample", "booleanExample"),
                exportStatement("IAnyExample", "anyExample"),
                exportStatement("IAnyMapExample", "anyMapExample"),
                exportStatement("IManyFieldExample", "manyFieldExample"),
                exportStatement("IUnionTypeExample", "unionTypeExample"),
                exportStatement("IUnionReferenceExample", "unionReferenceExample"));
    }

    private ExportStatement exportStatement(String typeName, String filename) {
        return ExportStatement.builder()
                .addNamesToExport(typeName)
                .filepathToExport("./test/api/" + filename)
                .build();
    }
}

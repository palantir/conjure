/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.io.CharStreams;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public final class ConjureTypescriptTypeTest {
    @Test
    public void testTypescriptTypeGenerator_allExamples() throws IOException {
        ConjureDefinition def = Conjure.parse(getClass().getResourceAsStream("/example-types.yml"));

        Set<TypescriptFile> files = new DefaultTypeGenerator().generate(def.types());

        for (TypescriptFile file : files) {
            assertThat(file.writeToString()).isEqualTo(CharStreams.toString(new InputStreamReader(
                    getClass().getResourceAsStream("/types/" + StringUtils.uncapitalize(file.name()) + ".ts"),
                    StandardCharsets.UTF_8)));
        }
    }
}

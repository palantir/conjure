/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.conjure.parser.types.names.Namespace;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class ConjureParserTest {

    @Test
    public void testConjureInlinedImports() throws IOException {
        ConjureDefinition conjure = ConjureParser.parse(new File("src/test/resources/example-conjure-imports.yml"));
        assertThat(conjure.types().conjureImports()).containsKey(Namespace.of("imports"));
    }

    @Test
    public void cyclicImportsAreNotAllowed() throws IOException {
        assertThatThrownBy(() -> ConjureParser.parse(new File("src/test/resources/example-recursive-imports.yml")))
                .isInstanceOf(ConjureParser.CyclicImportException.class);
    }

    @Test
    public void duplicate_keys_fail_to_parse() throws Exception {
        assertThatThrownBy(() -> ConjureParser.parse(new File("src/test/resources/duplicate-keys.yml")))
                .hasMessageContaining("Duplicate field 'services'");
    }
}

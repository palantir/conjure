/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.types.names.Namespace;
import com.palantir.conjure.defs.types.reference.ImportedTypes;
import java.io.File;
import org.junit.Test;

public class ConjureTest {

    @Test
    public void testImportsAreNotInlinedRecursively() throws Exception {
        ConjureDefinition definition = Conjure.parse(new File("src/test/resources/example-recursive-imports.yml"));
        ImportedTypes imports = definition.types().conjureImports().get(Namespace.of("imports"));
        assertThat(imports.importedTypes().objects()).isNotEmpty();
    }
}

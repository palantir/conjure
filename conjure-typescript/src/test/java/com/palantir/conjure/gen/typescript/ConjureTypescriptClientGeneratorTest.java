/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import static org.assertj.core.api.Assertions.assertThat;

import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class ConjureTypescriptClientGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void nativeTypesTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/native-types.conjure"));
        File src = folder.newFolder("src");
        ConjureTypescriptClientGenerator generator = new ConjureTypescriptClientGenerator(
                new DefaultServiceGenerator(),
                new DefaultTypeGenerator());
        generator.emit(conjure, src);
        String xfile = "package/x.ts";
        assertThat(compiledFile(src, xfile))
                .contains("interface IX");
        assertThat(compiledFile(src, xfile))
                .contains("fdouble: number");
        assertThat(compiledFile(src, xfile))
                .contains("finteger: number");
        assertThat(compiledFile(src, xfile))
                .contains("fmap: { [key: string]: string }");
        assertThat(compiledFile(src, xfile))
                .contains("fstring: string");
        assertThat(compiledFile(src, xfile))
                .contains("foptional?: string");
    }

    @Test
    public void referenceTypesTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/reference-types.conjure"));
        File src = folder.newFolder("src");
        ConjureTypescriptClientGenerator generator = new ConjureTypescriptClientGenerator(
                new DefaultServiceGenerator(),
                new DefaultTypeGenerator());
        generator.emit(conjure, src);
        String xfile = "package1/x.ts";
        String yfile = "package1/folder/y.ts";
        String zfile = "package2/folder/z.ts";

        // Assert all files are generated
        assertThat(compiledFile(src, xfile))
                .contains("interface IX");
        assertThat(compiledFile(src, yfile))
                .contains("interface IY");
        assertThat(compiledFile(src, zfile))
                .contains("interface IZ");

        // Assert expected references to Y, Z, and EnumObject from X
        assertThat(compiledFile(src, xfile))
                .contains("import { IY } from \"./folder/y\"");
        assertThat(compiledFile(src, xfile))
                .contains("import { IZ } from \"../package2/folder/z\"");
        assertThat(compiledFile(src, xfile))
                .contains("import { EnumObject } from \"./enumObject\"");
    }

    private static String compiledFile(File srcDir, String clazz) throws IOException {
        return GenerationUtils.getCharSource(new File(srcDir, clazz));
    }
}

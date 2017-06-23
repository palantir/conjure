/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.services.DefaultServiceGenerator;
import com.palantir.conjure.gen.typescript.types.DefaultTypeGenerator;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class ConjureTypescriptClientGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File src;
    private ConjureTypeScriptClientGenerator generator;

    @Before
    public void before() throws IOException {
        src = folder.newFolder("src");
        generator = new ConjureTypeScriptClientGenerator(
                new DefaultServiceGenerator(),
                new DefaultTypeGenerator());
    }

    @Test
    public void nativeTypesTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/native-types.conjure"));
        generator.emit(ImmutableList.of(conjure), "0.0.0", src);
        String xfile = "@palantir/package/foo.ts";
        assertThat(compiledFileContent(src, xfile))
                .contains("interface IFoo");
        assertThat(compiledFileContent(src, xfile))
                .contains("fdouble: number");
        assertThat(compiledFileContent(src, xfile))
                .contains("finteger: number");
        assertThat(compiledFileContent(src, xfile))
                .contains("fmap: { [key: string]: string }");
        assertThat(compiledFileContent(src, xfile))
                .contains("fstring: string");
        assertThat(compiledFileContent(src, xfile))
                .contains("foptional?: string");
    }

    @Test
    public void referenceTypesTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/reference-types.conjure"));
        generator.emit(ImmutableList.of(conjure), "0.0.0", src);
        String fooFile = "@palantir/package1/foo.ts";
        String barFile = "@palantir/package1-folder/bar.ts";
        String boomFile = "@palantir/package2-folder/boom.ts";

        // Assert all files are generated
        assertThat(compiledFileContent(src, fooFile))
                .contains("interface IFoo");
        assertThat(compiledFileContent(src, barFile))
                .contains("interface IBar");
        assertThat(compiledFileContent(src, boomFile))
                .contains("interface IBoom");

        // Assert expected references to Bar/Boom, and EnumObject from Foo
        assertThat(compiledFileContent(src, fooFile))
                .contains("import { IBar } from \"@palantir/package1-folder\"");
        assertThat(compiledFileContent(src, fooFile))
                .contains("import { IBoom } from \"@palantir/package2-folder\"");
        assertThat(compiledFileContent(src, fooFile))
                .contains("import { EnumObject } from \"./enumObject\"");
    }

    @Test
    public void indexFileTest() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/services/test-service.yml"));
        generator.emit(ImmutableList.of(conjure), "0.0.0", src);

        assertThat(compiledFileContent(src, "@palantir/foundry-catalog-api/index.ts")).isEqualTo(
                "export { ICreateDatasetRequest } from \"./createDatasetRequest\";\n"
                + "export { ITestService } from \"./testService\";\n"
                + "export { TestService } from \"./testServiceImpl\";\n");

        assertThat(compiledFileContent(src, "@palantir/foundry-catalog-api-datasets/index.ts")).isEqualTo(
                "export { IBackingFileSystem } from \"./backingFileSystem\";\n"
                + "export { IDataset } from \"./dataset\";\n");
    }

    @Test
    public void indexFileTest_multipleConjureDefinitions() throws IOException {
        ConjureDefinition conjure1 = Conjure.parse(new File("src/test/resources/multiple-conjure-files-1.yml"));
        ConjureDefinition conjure2 = Conjure.parse(new File("src/test/resources/multiple-conjure-files-2.yml"));
        List<ConjureDefinition> definitions = ImmutableList.of(conjure1, conjure2);
        generator.emit(definitions, "0.0.0", src);

        assertThat(compiledFileContent(src, "@palantir/test-api-objects/index.ts")).isEqualTo(
                new String(Files.readAllBytes(
                        new File("src/test/resources/multiple-conjure-files-index.ts").toPath()),
                        StandardCharsets.UTF_8));
    }

    @Test
    public void testConjureImports() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        generator.emit(ImmutableList.of(conjure), "0.0.0", src);

        // Generated files contain imports
        assertThat(compiledFileContent(src, "@api/with-imports/complexObjectWithImports.ts"))
                .contains("import { IStringExample } from \"@palantir/test-api\"")
                .doesNotContain("import { string }");
        assertThat(compiledFileContent(src, "@api/with-imports/testService.ts"))
                .contains("import { IBackingFileSystem } from \"@palantir/foundry-catalog-api-datasets\"")
                .contains("import { IStringExample } from \"@palantir/test-api\"");

        // Imported files are not generated
        assertThat(new File(src, "@palantir/foundry-catalog-api-datasets/backingFileSystem.ts")).doesNotExist();
        assertThat(new File(src, "test/api/stringExample.ts")).doesNotExist();
    }

    @Test
    public void testPackageJson() throws IOException {
        ConjureDefinition conjure = Conjure.parse(new File("src/test/resources/example-conjure-imports.yml"));
        generator.emit(ImmutableList.of(conjure), "0.0.0", src);

        // first package
        File packageJsonFile = new File(src, "@api/with-imports/package.json");
        assertThat(packageJsonFile).exists();
        ObjectMapper mapper = new ObjectMapper();
        assertThat(mapper.readValue(packageJsonFile, PackageJson.class)).isEqualTo(
                PackageJson.builder()
                        .author("Palantir Technologies, Inc.")
                        .putPeerDependencies(ConjureTypeScriptClientGenerator.CONJURE_FE_LIB,
                                ConjureTypeScriptClientGenerator.CONJURE_FE_LIB_VERSION)
                        .version("0.0.0")
                        .description("Conjure definitions for @api/with-imports")
                        .name("@api/with-imports")
                        .putDependencies("@palantir/test-api", "^0.0.0")
                        .putDependencies("@palantir/foundry-catalog-api-datasets", "^0.0.0")
                        .build());

        // second package
        packageJsonFile = new File(src, "@api/with-imports-package-override/package.json");
        assertThat(packageJsonFile).exists();
        assertThat(mapper.readValue(packageJsonFile, PackageJson.class)).isEqualTo(
                PackageJson.builder()
                        .author("Palantir Technologies, Inc.")
                        .putPeerDependencies(ConjureTypeScriptClientGenerator.CONJURE_FE_LIB,
                                ConjureTypeScriptClientGenerator.CONJURE_FE_LIB_VERSION)
                        .version("0.0.0")
                        .description("Conjure definitions for @api/with-imports-package-override")
                        .name("@api/with-imports-package-override")
                        .putDependencies("@palantir/test-api", "^0.0.0")
                        .build());
    }

    private static String compiledFileContent(File srcDir, String clazz) throws IOException {
        return GenerationUtils.getCharSource(new File(srcDir, clazz));
    }

}

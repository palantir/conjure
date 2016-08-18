/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.staticgen;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.typescript.poet.RawEmittable;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import com.palantir.conjure.gen.typescript.poet.TypescriptType;
import com.palantir.conjure.gen.typescript.utils.GenerationUtils;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public final class StaticGenerator {
    public Set<TypescriptFile> generate(ConjureDefinition conjureDefinition) {
        try {
            String httpApiBridge = GenerationUtils.getCharSource(new File("src/main/resources/httpApiBridge.ts"));
            TypescriptFile httpApiBridgeFile = TypescriptFile.builder()
                    .name("httpApiBridge")
                    .addEmittables(RawEmittable.builder().content(httpApiBridge).build())
                    .parentFolderPath("static")
                    .build();

            String httpApiBridgeImpl = GenerationUtils.getCharSource(
                    new File("src/main/resources/httpApiBridgeImpl.ts"));
            Set<TypescriptType> typesToImport = Sets.newHashSet(TypescriptType.builder().name("IHttpApiBridge").build(),
                    TypescriptType.builder().name("IHttpEndpointOptions").build());
            TypescriptFile httpApiBridgeImplFile = TypescriptFile.builder()
                    .name("httpApiBridgeImpl")
                    .addImports(GenerationUtils.createImportStatement(typesToImport, "static", "httpApiBridgeImpl",
                            "static", "httpApiBridge"))
                    .addEmittables(RawEmittable.builder().content(httpApiBridgeImpl).build())
                    .parentFolderPath("static")
                    .build();

            String utils = GenerationUtils.getCharSource(new File("src/main/resources/utils.ts"));
            TypescriptFile utilsFile = TypescriptFile.builder()
                    .name("utils")
                    .addEmittables(RawEmittable.builder().content(utils).build())
                    .parentFolderPath("static")
                    .build();

            return Sets.newHashSet(httpApiBridgeFile, httpApiBridgeImplFile, utilsFile);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read static file");
        }
    }

    public void emit(ConjureDefinition conjureDefinition, File outputDir) {
        generate(conjureDefinition).forEach(f -> {
            try {
                f.writeTo(outputDir);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });
    }
}

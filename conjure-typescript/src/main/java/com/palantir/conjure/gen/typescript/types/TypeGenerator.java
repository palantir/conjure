/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.types;

import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface TypeGenerator {

    Set<TypescriptFile> generate(TypesDefinition types);

    /**
     * Returns a set of {@link ExportStatement}s for the given type definitions.
     * <p>
     * An exported name may be defined in more than one {@link ExportStatement}. Callers of this method are expected to
     * resolve or deconflict as appropriate.
     */
    Map<ConjurePackage, Collection<ExportStatement>> generateExports(TypesDefinition types);

    default void emit(TypesDefinition types, File outputDir) {
        generate(types).forEach(file -> {
            try {
                file.writeTo(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}

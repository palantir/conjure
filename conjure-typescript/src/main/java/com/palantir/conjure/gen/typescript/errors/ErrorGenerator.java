/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.errors;

import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ErrorGenerator {

    /**
     * Returns the set of Typescript files generated from the 'errors' definitions in the given conjure specification.
     */
    Set<TypescriptFile> generate(List<ErrorTypeDefinition> definitions);

    /**
     * Returns a set of {@link ExportStatement}s for the errors definitions in the given conjure specification.
     * <p>
     * An exported name may be defined in more than one {@link ExportStatement}. Callers of this method are expected to
     * resolve or deconflict as appropriate.
     */
    Map<ConjurePackage, Collection<ExportStatement>> generateExports(List<ErrorTypeDefinition> definitions);

}

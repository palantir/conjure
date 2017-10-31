/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.services;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.gen.typescript.poet.ExportStatement;
import com.palantir.conjure.gen.typescript.poet.TypescriptFile;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ServiceGenerator {

    /**
     * Returns the set of Typescript files generated from the service definitions
     * in the given conjure specification.
     */
    Set<TypescriptFile> generate(ConjureDefinition conjureDefinition);

    /**
     * Returns a set of {@link ExportStatement}s for the service definitions in the given conjure specification.
     * <p>
     * An exported name may be defined in more than one {@link ExportStatement}. Callers of this method are expected to
     * resolve or deconflict as appropriate.
     */
    Map<ConjurePackage, Collection<ExportStatement>> generateExports(ConjureDefinition conjureDefinition);

}

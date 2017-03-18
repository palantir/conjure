/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.ConjureImports;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.squareup.javapoet.JavaFile;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public interface ServiceGenerator {

    /** Returns the set of Java files generated from the service definitions in the given conjure specification. */
    Set<JavaFile> generate(ConjureDefinition conjureDefinition, ConjureImports imports);

    static Optional<String> getJavaDoc(EndpointDefinition endpointDef) {
        Optional<String> depr = endpointDef.deprecated()
                .map(v -> StringUtils.appendIfMissing("@deprecated " + v, "\n"));

        Optional<String> docs = endpointDef.docs()
                .map(v -> StringUtils.appendIfMissing(v, "\n"));

        StringBuilder sb = new StringBuilder();
        docs.ifPresent(sb::append);
        depr.ifPresent(sb::append);
        return sb.length() > 0 ? Optional.of(sb.toString()) : Optional.empty();
    }

}

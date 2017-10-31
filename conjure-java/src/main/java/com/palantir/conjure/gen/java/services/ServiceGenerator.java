/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.google.common.collect.Lists;
import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.defs.services.EndpointDefinition;
import com.palantir.conjure.gen.java.util.Goethe;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public interface ServiceGenerator {

    /** Returns the set of Java files generated from the service definitions in the given conjure specification. */
    Set<JavaFile> generate(ConjureDefinition conjureDefinition);

    /**
     * Generates and emits to the given output directory all services and types of the given conjure definition, using
     * the instance's service and type generators.
     */
    default List<Path> emit(ConjureDefinition conjureDefinition, File outputDir) {
        List<Path> emittedPaths = Lists.newArrayList();
        generate(conjureDefinition).forEach(f -> {
            Path emittedPath = Goethe.formatAndEmit(f, outputDir.toPath());
            emittedPaths.add(emittedPath);
        });
        return emittedPaths;
    }

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

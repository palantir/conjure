/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.services;

import com.google.common.base.Throwables;
import com.palantir.conjure.defs.ConjureDefinition;
import com.squareup.javapoet.JavaFile;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface ServiceGenerator {

    /** Returns the set of Java files generated from the service definitions in the given conjure specification. */
    Set<JavaFile> generate(ConjureDefinition conjureDefinition);

    /**
     * Writes the Java files generated from the service definitions in the given conjure specification to the given
     * directory.
     */
    default void emit(ConjureDefinition conjureDefinition, File outputDir) {
        generate(conjureDefinition).forEach(f -> {
            try {
                f.writeTo(outputDir);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        });
    }
}

/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.reference;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.ConjureDefinition;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureImports {

    /**
     * The file from which types are to be imported. The path is relative to the directory in which the declaring
     * top-level ConjureDefinition file lives.
     */
    String file();

    ConjureDefinition conjure();

    @JsonCreator
    static ConjureImports fromFile(String file) {
        return ImmutableConjureImports.builder()
                .file(file)
                // When deserializing this object from user-supplied conjure yaml, we just fill in the file.
                .conjure(ConjureDefinition.builder().build())
                .build();
    }

    static ConjureImports withResolvedImports(String file, ConjureDefinition conjureDefinition) {
        return ImmutableConjureImports.builder()
                .file(file)
                .conjure(conjureDefinition)
                .build();
    }
}

/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.palantir.conjure.parser.ConjureParser;
import java.io.File;

public final class Conjure {

    private Conjure() {}

    /**
     * Deserializes a {@link ConjureDefinition} from its YAML representation in the given file.
     */
    public static ConjureDefinition parse(File file) {
        return ConjureDefinition.fromParse(ConjureParser.parse(file));
    }
}

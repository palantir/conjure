/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.palantir.conjure.parser.ConjureParser;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

public final class Conjure {
    public static final Integer SUPPORTED_IR_VERSION = 1;

    private Conjure() {}

    /**
     * Deserializes {@link ConjureDefinition}s from their YAML representations in the given files.
     */
    public static ConjureDefinition parse(Collection<File> files) {
        return ConjureParserUtils.parseConjureDef(
                files.stream().map(ConjureParser::parse).collect(Collectors.toList()));
    }
}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class Conjure {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory())
            .registerModule(new Jdk8Module())
            .setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);

    private Conjure() {}

    public static ConjureDefinition parse(File file) {
        try {
            return MAPPER.readValue(file, ConjureDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ConjureDefinition parse(InputStream stream) {
        try {
            return MAPPER.readValue(stream, ConjureDefinition.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

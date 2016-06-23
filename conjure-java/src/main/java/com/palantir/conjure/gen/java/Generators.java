/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java;

import com.palantir.conjure.defs.ConjureDefinition;
import com.palantir.conjure.gen.java.services.JerseyServiceGenerator;
import java.io.File;

public final class Generators {

    private Generators() {}

    public static void generateJerseyServices(ConjureDefinition def, Settings settings, File outputDir) {
        settings.typeGenerator().get().emit(def.types(), settings, outputDir);
        new JerseyServiceGenerator(def, settings).emit(outputDir);
    }

}

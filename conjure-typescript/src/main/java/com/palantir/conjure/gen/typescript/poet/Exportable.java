/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.typescript.poet;

import java.util.Optional;

/**
 * Any {@link Emittable} that might also 'export' a typescript identifier should return that identifier
 * from the exportName method.
 */
public interface Exportable extends Emittable {
    Optional<String> exportName();
}

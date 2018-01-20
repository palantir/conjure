/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.types.names.TypeName;
import java.util.Optional;

public interface BaseObjectTypeDefinition {
    // TODO(rfink): Rename to "name"?
    TypeName typeName();
    Optional<String> docs();
}

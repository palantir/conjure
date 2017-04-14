/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.names.TypeName;

public interface ReferenceType extends ConjureType {
    TypeName type();
}

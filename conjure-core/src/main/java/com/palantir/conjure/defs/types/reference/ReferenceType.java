/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.names.TypeName;

public interface ReferenceType extends Type {
    TypeName type();
}

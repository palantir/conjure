/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.reference;

import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.names.TypeName;

public interface ReferenceType extends ConjureType {
    TypeName type();
}

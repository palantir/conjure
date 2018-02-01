/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface AliasTypeDefinition extends BaseObjectTypeDefinition {

    ConjureType alias();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}

}

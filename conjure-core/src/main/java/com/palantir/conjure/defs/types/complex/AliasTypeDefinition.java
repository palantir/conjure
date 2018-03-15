/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.TypeDefinition;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface AliasTypeDefinition extends TypeDefinition {

    Type alias();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}

}

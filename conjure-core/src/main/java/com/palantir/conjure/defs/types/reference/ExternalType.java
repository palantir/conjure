/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.Type;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ExternalType extends Type {

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitExternal(this);
    }

    TypeName externalReference();

    PrimitiveType fallback();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableExternalType.Builder {}
}

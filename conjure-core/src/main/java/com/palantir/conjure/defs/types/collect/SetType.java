/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.collect;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.Type;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface SetType extends Type {

    Type itemType();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitSet(this);
    }

    static SetType of(Type itemType) {
        return ImmutableSetType.builder().itemType(itemType).build();
    }

}

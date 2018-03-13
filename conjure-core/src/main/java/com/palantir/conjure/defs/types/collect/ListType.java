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
public interface ListType extends Type {

    Type itemType();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitList(this);
    }

    static ListType of(Type itemType) {
        return ImmutableListType.builder().itemType(itemType).build();
    }

}

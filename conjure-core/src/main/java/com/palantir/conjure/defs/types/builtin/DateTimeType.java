/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.builtin;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.Type;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface DateTimeType extends Type {

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitDateTime(this);
    }

    // marker interface

    static DateTimeType of() {
        return ImmutableDateTimeType.builder().build();
    }

}

/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.builtin;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface DateTimeType extends ConjureType {

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitDateTime(this);
    }

    // marker interface

    static DateTimeType of() {
        return ImmutableDateTimeType.builder().build();
    }

}

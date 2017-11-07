/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.names.Namespace;
import com.palantir.conjure.defs.types.names.TypeName;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ForeignReferenceType extends ReferenceType {

    Namespace namespace();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitForeignReference(this);
    }

    static ForeignReferenceType of(Namespace namespace, TypeName type) {
        return ImmutableForeignReferenceType.builder().namespace(namespace).type(type).build();
    }
}

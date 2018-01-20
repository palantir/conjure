/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
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

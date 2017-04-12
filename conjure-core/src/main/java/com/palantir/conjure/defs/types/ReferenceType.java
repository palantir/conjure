/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ReferenceType extends ConjureType {

    Optional<String> namespace();

    TypeName type();

    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static ReferenceType of(TypeName type) {
        Optional<PrimitiveType> primitiveType = PrimitiveType.fromTypeName(type);
        if (primitiveType.isPresent()) {
            return primitiveType.get();
        }
        return ImmutableReferenceType.builder().type(type).build();
    }

    static ReferenceType of(String namespace, TypeName type) {
        return ImmutableReferenceType.builder().namespace(namespace).type(type).build();
    }

}

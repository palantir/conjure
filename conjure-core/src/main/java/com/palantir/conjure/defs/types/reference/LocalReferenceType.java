/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.ConjureTypeVisitor;
import com.palantir.conjure.defs.types.TypesDefinition;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Represents a reference to a builtin type or a type defined local to the containing
 * {@link com.palantir.conjure.defs.ConjureDefinition} (as opposed to a {@link ForeignReferenceType} which references a
 * type defined in an {@link TypesDefinition#conjureImports imported type}.
 */
@Value.Immutable
@ConjureImmutablesStyle
public interface LocalReferenceType extends ReferenceType {
    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static LocalReferenceType of(TypeName type) {
        Optional<PrimitiveType> primitiveType = PrimitiveType.fromTypeName(type);
        if (primitiveType.isPresent()) {
            return primitiveType.get();
        }
        return ImmutableLocalReferenceType.builder().type(type).build();
    }
}

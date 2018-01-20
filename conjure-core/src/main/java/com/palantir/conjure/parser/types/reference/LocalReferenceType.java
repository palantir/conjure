/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.ConjureDefinition;
import com.palantir.conjure.parser.types.ConjureTypeVisitor;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import java.util.Optional;
import org.immutables.value.Value;

/**
 * Represents a reference to a builtin type or a type defined local to the containing {@link ConjureDefinition} (as
 * opposed to a {@link ForeignReferenceType} which references a type defined in an {@link
 * com.palantir.conjure.parser.types.TypesDefinition#conjureImports imported type}).
 */
@Value.Immutable
@ConjureImmutablesStyle
public interface LocalReferenceType extends ReferenceType {
    @Override
    default <T> T visit(ConjureTypeVisitor<T> visitor) {
        return visitor.visitLocalReference(this);
    }

    static LocalReferenceType of(TypeName type) {
        Optional<PrimitiveType> primitiveType = PrimitiveType.fromTypeName(type);
        if (primitiveType.isPresent()) {
            return primitiveType.get();
        }
        return ImmutableLocalReferenceType.builder().type(type).build();
    }
}

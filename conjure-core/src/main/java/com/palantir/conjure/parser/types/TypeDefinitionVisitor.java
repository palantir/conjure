/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types;

import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;

public interface TypeDefinitionVisitor<T> {
    T visit(AliasTypeDefinition def);
    T visit(EnumTypeDefinition def);
    T visit(ObjectTypeDefinition def);
    T visit(UnionTypeDefinition def);
}

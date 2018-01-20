/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.reference;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureType;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.names.TypeName;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface AliasTypeDefinition extends BaseObjectTypeDefinition {

    ConjureType alias();

    static BaseObjectTypeDefinition parseFrom(
            TypeName name,
            com.palantir.conjure.parser.types.reference.AliasTypeDefinition def,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        return builder()
                .typeName(name)
                .alias(def.alias().visit(new ConjureTypeParserVisitor(typeResolver)))
                .docs(def.docs())
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}

}

/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.ConjureTypeParserVisitor;
import com.palantir.conjure.defs.types.ObjectTypeDefParserVisitor;
import com.palantir.conjure.defs.types.names.FieldName;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.Map;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface UnionTypeDefinition extends BaseObjectTypeDefinition {

    Map<FieldName, FieldDefinition> union();

    @Value.Check
    default void check() {
        for (UnionTypeDefinitionValidator validator : UnionTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static BaseObjectTypeDefinition parseFrom(
            TypeName name,
            com.palantir.conjure.parser.types.complex.UnionTypeDefinition def,
            ConjureTypeParserVisitor.TypeNameResolver typeResolver) {
        return builder()
                .typeName(name)
                .union(ObjectTypeDefParserVisitor.parseFieldDef(def.union(), typeResolver))
                .docs(def.docs())
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableUnionTypeDefinition.Builder {}

}

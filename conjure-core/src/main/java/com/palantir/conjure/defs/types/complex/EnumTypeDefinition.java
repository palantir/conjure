/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.TypeName;
import java.util.List;
import java.util.stream.Collectors;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface EnumTypeDefinition extends BaseObjectTypeDefinition {

    List<EnumValueDefinition> values();

    @Value.Check
    default void check() {
        for (ConjureValidator<EnumTypeDefinition> validator : EnumTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static BaseObjectTypeDefinition parseFrom(
            TypeName name,
            com.palantir.conjure.parser.types.complex.EnumTypeDefinition def) {
        return builder()
                .typeName(name)
                .values(def.values().stream().map(EnumValueDefinition::parseFrom).collect(Collectors.toList()))
                .docs(def.docs())
                .build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumTypeDefinition.Builder {}
}

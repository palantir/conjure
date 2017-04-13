/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.ConjureValidator;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import java.io.IOException;
import java.util.List;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEnumTypeDefinition.class)
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

    static EnumTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, EnumTypeDefinition.class);
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumTypeDefinition.Builder {}
}

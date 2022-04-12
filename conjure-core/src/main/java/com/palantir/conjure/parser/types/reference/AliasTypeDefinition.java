/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.parser.types.reference;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.LogSafetyDefinition;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.TypeDefinitionVisitor;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableAliasTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface AliasTypeDefinition extends BaseObjectTypeDefinition {

    ConjureType alias();

    Optional<LogSafetyDefinition> safety();

    @Override
    default <T> T visit(TypeDefinitionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static Builder builder() {
        return new Builder();
    }

    static AliasTypeDefinition fromJson(JsonParser parser, TreeNode json) throws IOException {
        return parser.getCodec().treeToValue(json, AliasTypeDefinition.class);
    }

    class Builder extends ImmutableAliasTypeDefinition.Builder {}
}

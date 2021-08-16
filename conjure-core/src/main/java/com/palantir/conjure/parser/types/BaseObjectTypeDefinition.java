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

package com.palantir.conjure.parser.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.parser.types.BaseObjectTypeDefinition.BaseObjectTypeDefinitionDeserializer;
import com.palantir.conjure.parser.types.complex.EnumTypeDefinition;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.complex.ObjectTypeDefinition;
import com.palantir.conjure.parser.types.complex.UnionTypeDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.reference.AliasTypeDefinition;
import com.palantir.logsafe.exceptions.SafeIllegalArgumentException;
import java.io.IOException;
import java.util.Optional;

@JsonDeserialize(using = BaseObjectTypeDefinitionDeserializer.class)
public interface BaseObjectTypeDefinition {

    @JsonProperty("package")
    Optional<ConjurePackage> conjurePackage();

    Optional<String> docs();

    <T> T visit(TypeDefinitionVisitor<T> visitor);

    final class BaseObjectTypeDefinitionDeserializer extends JsonDeserializer<BaseObjectTypeDefinition> {
        @Override
        public BaseObjectTypeDefinition deserialize(JsonParser parser, DeserializationContext _ctxt)
                throws IOException {
            TreeNode tree = parser.readValueAsTree();
            if (tree.get("fields") != null) {
                return ObjectTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("values") != null) {
                return EnumTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("alias") != null) {
                return AliasTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("union") != null) {
                return UnionTypeDefinition.fromJson(parser, tree);
            } else if (tree.get("namespace") != null) {
                return ErrorTypeDefinition.fromJson(parser, tree);
            } else {
                throw new SafeIllegalArgumentException(
                        "Unrecognized definition, types must have either fields, values or an alias defined.");
            }
        }
    }
}

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableExternalTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ExternalTypeDefinition {

    Map<String, String> external();

    @JsonProperty("base-type")
    @Value.Default
    default PrimitiveType baseType() {
        return PrimitiveType.ANY;
    }

    static ExternalTypeDefinition javaType(String external, PrimitiveType baseType) {
        return ImmutableExternalTypeDefinition.builder()
                .putExternal("java", external)
                .baseType(baseType)
                .build();
    }

}

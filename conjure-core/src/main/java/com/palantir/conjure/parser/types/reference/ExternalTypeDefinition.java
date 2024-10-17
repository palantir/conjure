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
import com.palantir.conjure.parser.LogSafetyDefinition;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.ExternalImportDefinition;
import com.palantir.conjure.parser.types.primitive.PrimitiveType;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableExternalTypeDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ExternalTypeDefinition {

    ExternalImportDefinition external();

    @JsonProperty("base-type")
    @Value.Default
    default ConjureType baseType() {
        return PrimitiveType.ANY;
    }

    @JsonProperty("safety")
    Optional<LogSafetyDefinition> safety();

    static ExternalTypeDefinition javaType(String external, PrimitiveType baseType) {
        return ImmutableExternalTypeDefinition.builder()
                .external(ExternalImportDefinition.of(external))
                .baseType(baseType)
                .build();
    }
}

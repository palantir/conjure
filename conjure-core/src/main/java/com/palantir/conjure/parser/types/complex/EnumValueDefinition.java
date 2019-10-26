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

package com.palantir.conjure.parser.types.complex;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.EnumValueDefinition.EnumValueDeserializer;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = EnumValueDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EnumValueDefinition {

    String value();

    Optional<String> docs();

    static EnumValueDefinition.Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEnumValueDefinition.Builder {}

    class EnumValueDeserializer extends JsonDeserializer<EnumValueDefinition> {
        @SuppressWarnings("deprecation")
        @Override
        public EnumValueDefinition deserialize(JsonParser parser, DeserializationContext _ctxt) throws IOException {
            String candidate = parser.getValueAsString();
            if (candidate != null) {
                return builder().value(candidate).build();
            }

            return ImmutableEnumValueDefinition.fromJson(
                    parser.readValueAs(ImmutableEnumValueDefinition.Json.class));
        }
    }

}

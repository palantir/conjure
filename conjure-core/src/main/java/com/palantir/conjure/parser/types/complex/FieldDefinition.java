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
import com.palantir.conjure.parser.LogSafetyDefinition;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.conjure.parser.types.complex.FieldDefinition.FieldDefinitionDeserializer;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = FieldDefinitionDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface FieldDefinition {

    ConjureType type();

    Optional<String> docs();

    Optional<String> deprecated();

    Optional<LogSafetyDefinition> safety();

    static FieldDefinition of(ConjureType type) {
        return ImmutableFieldDefinition.builder().type(type).build();
    }

    // solve Jackson sad-times for multiple parser
    final class FieldDefinitionDeserializer extends JsonDeserializer<FieldDefinition> {
        @SuppressWarnings({"deprecation", "for-rollout:SuppressWarningsWithoutExplanation"})
        @Override
        public FieldDefinition deserialize(JsonParser parser, DeserializationContext _context) throws IOException {

            String candidate = parser.getValueAsString();
            if (candidate != null) {
                try {
                    return of(ConjureType.fromString(candidate));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            return ImmutableFieldDefinition.fromJson(parser.readValueAs(ImmutableFieldDefinition.Json.class));
        }
    }
}

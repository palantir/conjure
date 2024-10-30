/*
 * (c) Copyright 2024 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure.parser.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.services.EndpointError.EndpointErrorDeserializer;
import com.palantir.conjure.parser.types.ConjureType;
import com.palantir.parsec.ParseException;
import java.io.IOException;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(using = EndpointErrorDeserializer.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointError {
    ConjureType error();

    Optional<String> docs();

    static EndpointError of(ConjureType errorName) {
        return ImmutableEndpointError.builder().error(errorName).build();
    }

    // TODO(pm): see if there's something we can do with @JsonCreator.
    final class EndpointErrorDeserializer extends JsonDeserializer<EndpointError> {
        @Override
        public EndpointError deserialize(JsonParser parser, DeserializationContext _context) throws IOException {

            String candidate = parser.getValueAsString();
            if (candidate != null) {
                try {
                    return EndpointError.of(ConjureType.fromString(candidate));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            return ImmutableEndpointError.fromJson(parser.readValueAs(ImmutableEndpointError.Json.class));
        }
    }
}

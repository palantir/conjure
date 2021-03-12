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

package com.palantir.conjure.parser.services;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.ConjureType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableEndpointDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface EndpointDefinition {

    RequestLineDefinition http();

    Optional<AuthDefinition> auth();

    Map<ParameterName, ArgumentDefinition> args();

    Set<String> tags();

    Set<ConjureType> markers();

    Optional<ConjureType> returns();

    Optional<String> docs();

    Optional<String> deprecated();

    Set<ConjureType> errors();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableEndpointDefinition.Builder {}
}

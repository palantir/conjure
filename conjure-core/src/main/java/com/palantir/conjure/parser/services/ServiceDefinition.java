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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableServiceDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ServiceDefinition {

    /**
     * Human-readable name of the service.
     *
     * @deprecated unused and will be removed in the future
     */
    // TODO(rfink): This is unused. Remove?
    @Deprecated
    @JsonProperty("name")
    String doNotUseName();

    @JsonProperty("package")
    ConjurePackage conjurePackage();

    Optional<String> docs();

    @JsonProperty("default-auth")
    @Value.Default
    default AuthDefinition defaultAuth() {
        return AuthDefinition.none();
    }

    @JsonProperty("base-path")
    @Value.Default
    default PathString basePath() {
        return PathString.of("/");
    }

    Map<String, EndpointDefinition> endpoints();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableServiceDefinition.Builder {}

}

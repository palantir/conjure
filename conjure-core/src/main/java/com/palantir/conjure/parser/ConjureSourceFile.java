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

package com.palantir.conjure.parser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.services.ServiceDefinition;
import com.palantir.conjure.parser.types.TypesDefinition;
import com.palantir.conjure.parser.types.names.TypeName;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableConjureSourceFile.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureSourceFile {

    @Value.Default
    default TypesDefinition types() {
        return TypesDefinition.builder().build();
    }

    Map<TypeName, ServiceDefinition> services();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableConjureSourceFile.Builder {}
}

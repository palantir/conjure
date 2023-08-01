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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.ConstantDefinition;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.names.TypeName;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableNamedTypesDefinition.class)
@JsonSerialize(as = ImmutableNamedTypesDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface NamedTypesDefinition {

    @JsonProperty("default-package")
    Optional<ConjurePackage> defaultConjurePackage();

    Map<TypeName, BaseObjectTypeDefinition> objects();

    Map<TypeName, ErrorTypeDefinition> errors();

    Map<TypeName, ConstantDefinition> constants();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableNamedTypesDefinition.Builder {}
}

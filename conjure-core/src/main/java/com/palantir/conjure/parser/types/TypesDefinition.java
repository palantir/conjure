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
import com.palantir.conjure.parser.types.names.Namespace;
import com.palantir.conjure.parser.types.names.TypeName;
import com.palantir.conjure.parser.types.reference.ConjureImports;
import com.palantir.conjure.parser.types.reference.ExternalTypeDefinition;
import java.util.Map;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableTypesDefinition.class)
@JsonSerialize(as = ImmutableTypesDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface TypesDefinition {

    Map<TypeName, ExternalTypeDefinition> imports();

    /**
     * A list of Conjure definitions from which Conjure types are made available ("imported") for this Conjure
     * definition. For each import entry {@code <namespace>:<import-path>}, the Conjure compiler expects the existence
     * of a Conjure file {@code <import-path>} and makes each {@code <type>} imported from this Conjure definition
     * available as {@code <namespace>.<type>}.
     */
    @JsonProperty("conjure-imports")
    Map<Namespace, ConjureImports> conjureImports();

    @Value.Default
    default NamedTypesDefinition definitions() {
        return NamedTypesDefinition.builder().build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}

}

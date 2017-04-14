/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.Namespace;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
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
    Map<Namespace, String> conjureImports();

    @Value.Default
    default ObjectsDefinition definitions() {
        return ObjectsDefinition.builder().defaultConjurePackage(ConjurePackage.NONE).build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}

}

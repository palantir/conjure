/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Verify;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.names.ConjurePackage;
import com.palantir.conjure.defs.types.names.Namespace;
import com.palantir.conjure.defs.types.names.TypeName;
import com.palantir.conjure.defs.types.reference.ExternalTypeDefinition;
import com.palantir.conjure.defs.types.reference.ImportedTypes;
import com.palantir.conjure.defs.types.reference.ReferenceType;
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
    Map<Namespace, ImportedTypes> conjureImports();

    @Value.Lazy
    default ImportedTypes getImportsForRefNameSpace(ReferenceType type) {
        // TODO(rfink): Introduce ExternalReferenceType to obviate this check.
        Verify.verify(type.namespace().isPresent(), "Cannot call getImportsForRefNameSpace for non-import types");
        return Verify.verifyNotNull(conjureImports().get(type.namespace().get()),
                "No imported namespace found for reference type: %s", type);
    }

    @Value.Default
    default ObjectsDefinition definitions() {
        return ObjectsDefinition.builder().defaultConjurePackage(ConjurePackage.NONE).build();
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableTypesDefinition.Builder {}

}

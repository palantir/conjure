/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.parser.types.complex.ErrorTypeDefinition;
import com.palantir.conjure.parser.types.names.ConjurePackage;
import com.palantir.conjure.parser.types.names.TypeName;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@JsonDeserialize(as = ImmutableObjectsDefinition.class)
@JsonSerialize(as = ImmutableObjectsDefinition.class)
@Value.Immutable
@ConjureImmutablesStyle
public interface ObjectsDefinition {

    @JsonProperty("default-package")
    Optional<ConjurePackage> defaultConjurePackage();

    // TODO(rfink): Rename to "types" [backcompat break], https://github.palantir.build/foundry/conjure/issues/351
    Map<TypeName, BaseObjectTypeDefinition> objects();

    Map<TypeName, ErrorTypeDefinition> errors();

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableObjectsDefinition.Builder {}
}

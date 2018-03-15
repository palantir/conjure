/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.palantir.conjure.defs.services.ServiceDefinition;
import com.palantir.conjure.defs.types.TypeDefinition;
import com.palantir.conjure.defs.types.complex.ErrorTypeDefinition;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ConjureDefinition {

    List<TypeDefinition> types();

    List<ErrorTypeDefinition> errors();

    List<ServiceDefinition> services();

    @Value.Check
    default void check() {
        for (ConjureDefinitionValidator validator : ConjureDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableConjureDefinition.Builder {}

}

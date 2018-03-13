/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.types.complex;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import com.palantir.conjure.defs.types.BaseObjectTypeDefinition;
import com.palantir.conjure.defs.types.names.ErrorCode;
import com.palantir.conjure.defs.types.names.ErrorNamespace;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface ErrorTypeDefinition extends BaseObjectTypeDefinition {

    ErrorNamespace namespace();

    ErrorCode code();

    List<FieldDefinition> safeArgs();

    List<FieldDefinition> unsafeArgs();

    @Value.Check
    default void check() {
        for (ErrorTypeDefinitionValidator validator : ErrorTypeDefinitionValidator.values()) {
            validator.validate(this);
        }
    }

    static Builder builder() {
        return new Builder();
    }

    class Builder extends ImmutableErrorTypeDefinition.Builder {}

}

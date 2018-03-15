/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.palantir.conjure.defs.ConjureImmutablesStyle;
import org.immutables.value.Value;

@Value.Immutable
@ConjureImmutablesStyle
public interface HeaderParameterType extends ParameterType {

    ParameterId paramId();

    static HeaderParameterType header(String paramId) {
        return ImmutableHeaderParameterType.builder()
                .paramId(ParameterId.of(paramId)).build();
    }
}

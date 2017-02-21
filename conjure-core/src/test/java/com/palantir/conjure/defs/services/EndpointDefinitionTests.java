/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.types.BinaryType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class EndpointDefinitionTests {

    private static final EndpointDefinition BASE_DEF = EndpointDefinition.builder()
            .http(RequestLineDefinition.of("GET", "/"))
            .build();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCheck_binaryArgument() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Endpoint cannot have argument with type 'BinaryType{}'");
        EndpointDefinition.builder()
                .from(BASE_DEF)
                .args(ImmutableMap.of("arg", ArgumentDefinition.of(BinaryType.of())))
                .build();
    }

}

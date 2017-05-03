/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.types.builtin.AnyType;
import javax.ws.rs.HttpMethod;
import org.junit.Test;

public final class ParameterNameValidatorTest {

    @Test
    public void testValid() {
        for (String parameterName : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234")) {
            EndpointDefinition.Builder endpoint = createEndpoint(parameterName);
            // Passes validation
            endpoint.build();
        }
    }

    @Test
    public void testInvalid() {
        for (String parameterName : ImmutableList.of("AB", "123", "foo_bar", "foo-bar", "foo.bar")) {
            EndpointDefinition.Builder endpoint = createEndpoint(parameterName);
            assertThatThrownBy(endpoint::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter names in endpoint paths and service definitions must match pattern %s: %s",
                            ParameterName.ANCHORED_PATTERN,
                            parameterName);
        }
    }

    private EndpointDefinition.Builder createEndpoint(String parameterName) {
        ArgumentDefinition arg = ArgumentDefinition.of(AnyType.of());
        return EndpointDefinition.builder()
                .http(RequestLineDefinition.of(HttpMethod.POST, PathDefinition.of("/a/path")))
                .args(ImmutableMap.of(ParameterName.of(parameterName), arg));
    }
}

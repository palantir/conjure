/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import javax.ws.rs.HttpMethod;
import org.junit.Test;

public final class ParameterNameValidatorTest {

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValid() {
        for (String paramName : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234")) {
            EndpointDefinition.Builder endpoint = createEndpoint(paramName);
            // Passes validation
            endpoint.build();
        }
    }

    @Test
    public void testInvalid() {
        for (String paramName : ImmutableList.of("AB", "123", "foo_bar", "foo-bar", "foo.bar")) {
            EndpointDefinition.Builder endpoint = createEndpoint(paramName);
            assertThatThrownBy(endpoint::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter names in endpoint paths and service definitions must match pattern %s: %s",
                            ParameterName.ANCHORED_PATTERN,
                            paramName);
        }
    }

    private EndpointDefinition.Builder createEndpoint(String paramName) {
        ArgumentDefinition arg = ArgumentDefinition.builder()
                .paramId(ParameterName.of("foo"))
                .paramType(ArgumentDefinition.ParamType.PATH)
                .type(PrimitiveType.STRING)
                .build();
        return EndpointDefinition.builder()
                .auth(AuthDefinition.none())
                .http(RequestLineDefinition.of(HttpMethod.POST, PathDefinition.of("/a/path/{foo}")))
                .args(ImmutableMap.of(ParameterName.of(paramName), arg));
    }
}

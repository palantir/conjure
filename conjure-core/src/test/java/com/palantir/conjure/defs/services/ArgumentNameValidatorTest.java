/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.types.primitive.PrimitiveType;
import javax.ws.rs.HttpMethod;
import org.junit.Test;

public final class ArgumentNameValidatorTest {

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
                            ArgumentName.ANCHORED_PATTERN,
                            paramName);
        }
    }

    private EndpointDefinition.Builder createEndpoint(String paramName) {
        ArgumentDefinition arg = ArgumentDefinition.builder()
                .paramType(ArgumentDefinition.ParamType.BODY)
                .type(PrimitiveType.STRING)
                .argName(ArgumentName.of(paramName))
                .build();
        return EndpointDefinition.builder()
                .http(RequestLineDefinition.of(HttpMethod.POST, PathDefinition.of("/a/path")))
                .args(ImmutableList.of(arg))
                .endpointName(EndpointName.of("test"));
    }
}

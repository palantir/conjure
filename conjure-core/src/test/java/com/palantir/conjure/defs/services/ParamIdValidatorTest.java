/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import com.palantir.conjure.defs.types.builtin.AnyType;
import java.util.List;
import javax.ws.rs.HttpMethod;
import org.junit.Test;

public final class ParamIdValidatorTest {
    private static final ArgumentName PARAMETER_NAME = ArgumentName.of("arg");

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValidNonHeader() {
        for (String paramId : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234")) {
            EndpointDefinition.Builder endpoint = createEndpoint(ArgumentDefinition.ParamType.BODY, paramId);
            // Passes validation
            endpoint.build();
        }
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValidHeader() {
        List<String> paramIds = ImmutableList.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.X_XSS_PROTECTION,
                HttpHeaders.P3P,
                HttpHeaders.SET_COOKIE2);
        for (String paramId : paramIds) {
            EndpointDefinition.Builder endpoint =
                    createEndpoint(ArgumentDefinition.ParamType.HEADER, paramId);
            // Passes validation
            endpoint.build();
        }
    }

    @Test
    public void testInvalidNonHeader() {
        for (String paramId : ImmutableList.of("AB", "123", "foo_bar", "foo-bar", "foo.bar")) {
            EndpointDefinition.Builder endpoint =
                    createEndpoint(ArgumentDefinition.ParamType.BODY, paramId);
            assertThatThrownBy(endpoint::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter ids with type %s must match pattern %s: %s",
                            ArgumentDefinition.ParamType.BODY,
                            ArgumentName.ANCHORED_PATTERN,
                            paramId);
        }
    }

    @Test
    public void testInvalidHeader() {
        for (String paramId : ImmutableList.of("authorization", "123", "Foo_Bar", "Foo.Bar")) {
            EndpointDefinition.Builder endpoint =
                    createEndpoint(ArgumentDefinition.ParamType.HEADER, paramId);

            assertThatThrownBy(endpoint::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter ids with type %s must match pattern %s: %s",
                            ArgumentDefinition.ParamType.HEADER,
                            ParameterId.HEADER_PATTERN,
                            paramId);
        }
    }

    private EndpointDefinition.Builder createEndpoint(
            ArgumentDefinition.ParamType paramType,
            String paramId) {
        ArgumentDefinition arg = ArgumentDefinition.builder()
                .argName(PARAMETER_NAME)
                .paramType(paramType)
                .paramId(ParameterId.of(paramId))
                .type(AnyType.of())
                .build();
        return EndpointDefinition.builder()
                .http(RequestLineDefinition.of(HttpMethod.POST, PathDefinition.of("/a/path")))
                .addArgs(arg)
                .endpointName(EndpointName.of("test"));
    }
}

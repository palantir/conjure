/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HttpHeaders;
import com.palantir.conjure.defs.types.builtin.AnyType;
import java.util.List;
import org.junit.Test;

public final class ParamIdValidatorTest {
    private static final ArgumentName PARAMETER_NAME = ArgumentName.of("arg");

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValidNonHeader() {
        for (String paramId : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234")) {
            EndpointDefinition.Builder endpoint = createEndpoint(
                    QueryParameterType.query(paramId));
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
                    createEndpoint(HeaderParameterType.header(paramId));
            // Passes validation
            endpoint.build();
        }
    }

    @Test
    public void testInvalidNonHeader() {
        for (String paramId : ImmutableList.of("AB", "123", "foo_bar", "foo-bar", "foo.bar")) {
            ParameterType parameterType = QueryParameterType.query(paramId);
            EndpointDefinition.Builder endpoint = createEndpoint(parameterType);
            assertThatThrownBy(endpoint::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter ids with type %s must match pattern %s: %s",
                            parameterType,
                            ArgumentName.ANCHORED_PATTERN,
                            paramId);
        }
    }

    @Test
    public void testInvalidHeader() {
        for (String paramId : ImmutableList.of("authorization", "123", "Foo_Bar", "Foo.Bar")) {
            ParameterType parameterType = HeaderParameterType.header(paramId);
            EndpointDefinition.Builder endpoint = createEndpoint(parameterType);

            assertThatThrownBy(endpoint::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter ids with type %s must match pattern %s: %s",
                            parameterType,
                            ParameterId.HEADER_PATTERN,
                            paramId);
        }
    }

    private EndpointDefinition.Builder createEndpoint(ParameterType paramType) {
        ArgumentDefinition arg = ArgumentDefinition.builder()
                .argName(PARAMETER_NAME)
                .paramType(paramType)
                .type(AnyType.of())
                .build();
        return EndpointDefinition.builder()
                .httpMethod(EndpointDefinition.HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"))
                .addArgs(arg)
                .endpointName(EndpointName.of("test"));
    }
}

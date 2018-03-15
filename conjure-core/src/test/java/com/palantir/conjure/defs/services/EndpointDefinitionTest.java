/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import javax.ws.rs.HttpMethod;
import org.junit.Test;

public final class EndpointDefinitionTest {

    private static final EndpointName ENDPOINT_NAME = EndpointName.of("test");
    private static final RequestLineDefinition GET_REQUEST =
            RequestLineDefinition.of(HttpMethod.GET, PathDefinition.of("/a/path"));
    private static final ArgumentDefinition.Builder BODY_ARG_BUILDER = ArgumentDefinition.builder()
            .type(AnyType.of())
            .paramType(ArgumentDefinition.ParamType.BODY);

    @Test
    public void testArgumentTypeValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("testArg"))
                        .type(BinaryType.of())
                        .paramId(ParameterId.of("testArg"))
                        .paramType(ArgumentDefinition.ParamType.HEADER)
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .http(mock(RequestLineDefinition.class));

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Endpoint cannot have non-body argument with type 'BinaryType{}'");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testArgumentBodyTypeValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("testArg"))
                        .type(BinaryType.of())
                        .paramType(ArgumentDefinition.ParamType.BODY)
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .http(RequestLineDefinition.of(HttpMethod.POST, PathDefinition.of("/a/path")));

        // Should not throw exception
        definition.build();
    }

    @Test
    public void testSingleBodyParamValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .addArgs(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg1")).build())
                .addArgs(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg2")).build())
                .endpointName(ENDPOINT_NAME)
                .http(GET_REQUEST);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot have multiple body parameters: [bodyArg1, bodyArg2]");
    }

    @Test
    public void testPathParamValidatorUniquePathParams() {
        ArgumentDefinition paramDefinition1 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(AnyType.of())
                .paramType(ArgumentDefinition.ParamType.PATH)
                .build();
        ArgumentDefinition paramDefinition2 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(AnyType.of())
                .paramType(ArgumentDefinition.ParamType.PATH)
                .build();


        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(paramDefinition1, paramDefinition2))
                .endpointName(ENDPOINT_NAME)
                .http(GET_REQUEST);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameter with identifier \"paramName\" is defined multiple times for endpoint");
    }

    @Test
    public void testPathParamValidatorExtraParams() {
        ArgumentDefinition paramDefinition = ArgumentDefinition.builder()
                .type(AnyType.of())
                .argName(ArgumentName.of("paramName"))
                .paramType(ArgumentDefinition.ParamType.PATH)
                .build();

        RequestLineDefinition noParamRequest = RequestLineDefinition.of(HttpMethod.GET, PathDefinition.of("/a/path"));
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .addArgs(paramDefinition)
                .endpointName(ENDPOINT_NAME)
                .http(noParamRequest);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined in endpoint but not present in path template: [paramName]");
    }

    @Test
    public void testPathParamValidatorMissingParams() {
        RequestLineDefinition requestWithPathParam =
                RequestLineDefinition.of(HttpMethod.GET, PathDefinition.of("/a/path/{paramName}"));
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .http(requestWithPathParam);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined path template but not present in endpoint: [paramName]");
    }

    @Test
    public void testNoGetBodyValidator() {
        EndpointDefinition.Builder endpoint = EndpointDefinition.builder()
                .addArgs(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg")).build())
                .endpointName(ENDPOINT_NAME)
                .http(GET_REQUEST);

        assertThatThrownBy(endpoint::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot be a GET and contain a body: " + GET_REQUEST);
    }

}

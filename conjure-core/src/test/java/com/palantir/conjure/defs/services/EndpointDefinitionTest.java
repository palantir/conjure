/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import javax.ws.rs.HttpMethod;
import org.junit.Test;

public final class EndpointDefinitionTest {

    private static final RequestLineDefinition GET_REQUEST =
            RequestLineDefinition.of(HttpMethod.GET, PathDefinition.of("/a/path"));
    private static final ArgumentDefinition BODY_ARG = ArgumentDefinition.builder()
            .type(AnyType.of())
            .paramType(ArgumentDefinition.ParamType.BODY)
            .build();

    @Test
    public void testArgumentTypeValidator() throws Exception {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(map(ParameterName.of("testArg"), ArgumentDefinition.of(BinaryType.of())))
                .http(mock(RequestLineDefinition.class));

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Endpoint cannot have argument with type 'BinaryType{}'");
    }

    @Test
    public void testSingleBodyParamValidator() throws Exception {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(map(ParameterName.of("bodyArg1"), BODY_ARG, ParameterName.of("bodyArg2"), BODY_ARG))
                .http(GET_REQUEST);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot have multiple body parameters: [bodyArg1, bodyArg2]");
    }

    @Test
    public void testPathParamValidatorUniquePathParams() throws Exception {
        ArgumentDefinition paramDefinition = ArgumentDefinition.builder()
                .type(AnyType.of())
                .paramType(ArgumentDefinition.ParamType.PATH)
                .paramId(ParameterName.of("paramName"))
                .build();

        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(map(ParameterName.of("pathArg1"), paramDefinition, ParameterName.of("pathArg2"), paramDefinition))
                .http(GET_REQUEST);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameter with identifier \"paramName\" is defined multiple times for endpoint");
    }

    @Test
    public void testPathParamValidatorUniquePathParamsIdInferred() throws Exception {
        ArgumentDefinition namedParameter = ArgumentDefinition.builder()
                .type(AnyType.of())
                .paramType(ArgumentDefinition.ParamType.PATH)
                .paramId(ParameterName.of("paramName"))
                .build();
        ArgumentDefinition unNamedParameter = ArgumentDefinition.builder()
                .type(AnyType.of())
                .paramType(ArgumentDefinition.ParamType.PATH)
                .build();

        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(map(ParameterName.of("someName"), namedParameter, ParameterName.of("paramName"),
                        unNamedParameter))
                .http(GET_REQUEST);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameter with identifier \"paramName\" is defined multiple times for endpoint");
    }

    @Test
    public void testPathParamValidatorExtraParams() throws Exception {
        ArgumentDefinition paramDefinition = ArgumentDefinition.builder()
                .type(AnyType.of())
                .paramType(ArgumentDefinition.ParamType.PATH)
                .build();

        RequestLineDefinition noParamRequest = RequestLineDefinition.of(HttpMethod.GET, PathDefinition.of("/a/path"));
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(map(ParameterName.of("paramName"), paramDefinition))
                .http(noParamRequest);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined in endpoint but not present in path template: [paramName]");
    }

    @Test
    public void testPathParamValidatorMissingParams() throws Exception {
        RequestLineDefinition requestWithPathParam =
                RequestLineDefinition.of(HttpMethod.GET, PathDefinition.of("/a/path/{paramName}"));
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .http(requestWithPathParam);

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined path template but not present in endpoint: [paramName]");
    }

    @Test
    public void testNoGetBodyValidator() throws Exception {
        EndpointDefinition.Builder endpoint = EndpointDefinition.builder()
                .args(map(ParameterName.of("bodyArg"), BODY_ARG))
                .http(GET_REQUEST);

        assertThatThrownBy(endpoint::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot be a GET and contain a body: " + GET_REQUEST);
    }

    private static <K, V> ImmutableMap<K, V> map(K key, V value) {
        return ImmutableMap.of(key, value);
    }

    private static <K, V> ImmutableMap<K, V> map(K key, V value, K key2, V value2) {
        return ImmutableMap.of(key, value, key2, value2);
    }
}

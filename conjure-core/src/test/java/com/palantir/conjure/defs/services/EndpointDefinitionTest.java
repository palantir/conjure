/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.defs.types.builtin.AnyType;
import com.palantir.conjure.defs.types.builtin.BinaryType;
import org.junit.Test;

public final class EndpointDefinitionTest {

    private static final EndpointName ENDPOINT_NAME = EndpointName.of("test");
    private static final ArgumentDefinition.Builder BODY_ARG_BUILDER = ArgumentDefinition.builder()
            .type(AnyType.of())
            .paramType(BodyParameterType.body());

    @Test
    public void testArgumentTypeValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("testArg"))
                        .type(BinaryType.of())
                        .paramType(HeaderParameterType.header("testArg"))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

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
                        .paramType(BodyParameterType.body())
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"));

        // Should not throw exception
        definition.build();
    }

    @Test
    public void testSingleBodyParamValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .addArgs(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg1")).build())
                .addArgs(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg2")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot have multiple body parameters: [bodyArg1, bodyArg2]");
    }

    @Test
    public void testPathParamValidatorUniquePathParams() {
        ArgumentDefinition paramDefinition1 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(AnyType.of())
                .paramType(PathParameterType.path())
                .build();
        ArgumentDefinition paramDefinition2 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(AnyType.of())
                .paramType(PathParameterType.path())
                .build();


        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(paramDefinition1, paramDefinition2))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameter with identifier \"paramName\" is defined multiple times for endpoint");
    }

    @Test
    public void testPathParamValidatorExtraParams() {
        ArgumentDefinition paramDefinition = ArgumentDefinition.builder()
                .type(AnyType.of())
                .argName(ArgumentName.of("paramName"))
                .paramType(PathParameterType.path())
                .build();

        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .addArgs(paramDefinition)
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined in endpoint but not present in path template: [paramName]");
    }

    @Test
    public void testPathParamValidatorMissingParams() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path/{paramName}"));

        assertThatThrownBy(definition::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined path template but not present in endpoint: [paramName]");
    }

    @Test
    public void testNoGetBodyValidator() {
        EndpointDefinition.Builder endpoint = EndpointDefinition.builder()
                .addArgs(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(EndpointDefinition.HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(endpoint::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format(
                        "Endpoint cannot be a GET and contain a body: method: %s, path: %s",
                        EndpointDefinition.HttpMethod.GET,
                        "/a/path"));
    }

}

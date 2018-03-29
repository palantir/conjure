/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.BodyParameterType;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PathParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import org.junit.Test;

public final class EndpointDefinitionTest {

    private static final EndpointName ENDPOINT_NAME = EndpointName.of("test");
    private static final ArgumentDefinition.Builder BODY_ARG_BUILDER = ArgumentDefinition.builder()
            .type(Type.primitive(PrimitiveType.ANY))
            .paramType(ParameterType.body(BodyParameterType.of()));

    @Test
    public void testArgumentTypeValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("testArg"))
                        .type(Type.primitive(PrimitiveType.BINARY))
                        .paramType(ParameterType.header(HeaderParameterType.of(ParameterId.of("testArg"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Endpoint cannot have non-body argument with type "
                        + "'Type{value: PrimitiveWrapper{value: BINARY}}'");
    }

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testArgumentBodyTypeValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("testArg"))
                        .type(Type.primitive(PrimitiveType.BINARY))
                        .paramType(ParameterType.body(BodyParameterType.of()))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"));

        // Should not throw exception
        EndpointDefinitionValidator.validateAll(definition.build());
    }

    @Test
    public void testSingleBodyParamValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg1")).build())
                .args(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg2")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot have multiple body parameters: [bodyArg1, bodyArg2]");
    }

    @Test
    public void testPathParamValidatorUniquePathParams() {
        ArgumentDefinition paramDefinition1 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(Type.primitive(PrimitiveType.ANY))
                .paramType(ParameterType.path(PathParameterType.of()))
                .build();
        ArgumentDefinition paramDefinition2 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(Type.primitive(PrimitiveType.ANY))
                .paramType(ParameterType.path(PathParameterType.of()))
                .build();


        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(paramDefinition1, paramDefinition2))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameter with identifier \"paramName\" is defined multiple times for endpoint");
    }

    @Test
    public void testPathParamValidatorExtraParams() {
        ArgumentDefinition paramDefinition = ArgumentDefinition.builder()
                .type(Type.primitive(PrimitiveType.ANY))
                .argName(ArgumentName.of("paramName"))
                .paramType(ParameterType.path(PathParameterType.of()))
                .build();

        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(paramDefinition)
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined in endpoint but not present in path template: [paramName]");
    }

    @Test
    public void testPathParamValidatorMissingParams() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path/{paramName}"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameters defined path template but not present in endpoint: [paramName]");
    }

    @Test
    public void testNoGetBodyValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(String.format(
                        "Endpoint cannot be a GET and contain a body: method: %s, path: %s",
                        HttpMethod.GET,
                        "/a/path"));
    }

    @Test
    public void testComplexHeader() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("someName"))
                        .type(Type.primitive(PrimitiveType.ANY))
                        .paramType(ParameterType.header(HeaderParameterType.of(ParameterId.of("someId"))))
                        .build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Header parameters must be primitives, aliases or optional primitive:"
                        + " \"someName\" is not allowed");
    }
}

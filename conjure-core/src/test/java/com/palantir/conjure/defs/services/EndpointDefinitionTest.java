/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.types.ConjureType;
import java.util.Optional;
import javax.ws.rs.HttpMethod;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class EndpointDefinitionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testArgumentTypeValidator() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Endpoint cannot have argument with type 'BinaryType{}'");

        ArgumentDefinition binaryArg = mock(ArgumentDefinition.class);
        when(binaryArg.type()).thenReturn(ConjureType.fromString("binary"));

        EndpointDefinition endpoint = mock(EndpointDefinition.class);
        when(endpoint.args()).thenReturn(Optional.of(ImmutableMap.of("testArg", binaryArg)));

        EndpointDefinitionValidator.ARGUMENT_TYPE.validate(endpoint);
    }

    @Test
    public void testSingleBodyParamValidator() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Endpoint cannot have multiple body parameters: [bodyArg1, bodyArg2]");

        ArgumentDefinition bodyArg = mock(ArgumentDefinition.class);
        when(bodyArg.paramType()).thenReturn(ArgumentDefinition.ParamType.BODY);

        EndpointDefinition endpoint = mock(EndpointDefinition.class);
        when(endpoint.argsWithAutoDefined()).thenReturn(Optional.of(ImmutableMap.of(
                "bodyArg1", bodyArg,
                "bodyArg2", bodyArg
        )));

        EndpointDefinitionValidator.SINGLE_BODY_PARAM.validate(endpoint);
    }

    @Test
    public void testPathParamValidatorUniquePathParams() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "Path parameter with identifier \"paramName\" is defined multiple times for endpoint");

        ArgumentDefinition pathArgWithParamId = mock(ArgumentDefinition.class);
        when(pathArgWithParamId.paramType()).thenReturn(ArgumentDefinition.ParamType.PATH);
        when(pathArgWithParamId.paramId()).thenReturn(Optional.of("paramName"));

        EndpointDefinition endpoint = mock(EndpointDefinition.class);
        when(endpoint.argsWithAutoDefined()).thenReturn(Optional.of(ImmutableMap.of(
                "pathArg1", pathArgWithParamId,
                "pathArg2", pathArgWithParamId
        )));

        EndpointDefinitionValidator.PATH_PARAM.validate(endpoint);
    }

    @Test
    public void testPathParamValidatorUniquePathParamsIdInferred() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "Path parameter with identifier \"paramName\" is defined multiple times for endpoint");

        // path argument without an explicit paramId
        ArgumentDefinition pathArgNoParamId = mock(ArgumentDefinition.class);
        when(pathArgNoParamId.paramType()).thenReturn(ArgumentDefinition.ParamType.PATH);
        when(pathArgNoParamId.paramId()).thenReturn(Optional.empty());

        // path argument with explicit paramId of "paramName"
        ArgumentDefinition pathArgWithParamId = mock(ArgumentDefinition.class);
        when(pathArgWithParamId.paramType()).thenReturn(ArgumentDefinition.ParamType.PATH);
        when(pathArgWithParamId.paramId()).thenReturn(Optional.of("paramName"));

        EndpointDefinition endpoint = mock(EndpointDefinition.class);
        when(endpoint.argsWithAutoDefined()).thenReturn(Optional.of(ImmutableMap.of(
                "paramName", pathArgNoParamId,
                "pathArg2", pathArgWithParamId
        )));

        EndpointDefinitionValidator.PATH_PARAM.validate(endpoint);
    }

    @Test
    public void testPathParamValidatorExtraParams() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage(
                "Path parameters defined in endpoint but not present in path template: [paramName]");

        ArgumentDefinition pathArg = mock(ArgumentDefinition.class);
        when(pathArg.paramType()).thenReturn(ArgumentDefinition.ParamType.PATH);
        when(pathArg.paramId()).thenReturn(Optional.empty());

        // requestLine with no path parameters
        RequestLineDefinition requestLine = mock(RequestLineDefinition.class);
        when(requestLine.pathArgs()).thenReturn(ImmutableSet.of());

        EndpointDefinition endpoint = mock(EndpointDefinition.class);
        when(endpoint.argsWithAutoDefined()).thenReturn(Optional.of(ImmutableMap.of(
                "paramName", pathArg
        )));
        when(endpoint.http()).thenReturn(requestLine);

        EndpointDefinitionValidator.PATH_PARAM.validate(endpoint);
    }

    @Test
    public void testPathParamValidatorMissingParams() throws Exception {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Path parameters defined path template but not present in endpoint: [pathArg]");

        // requestLine with no path parameters
        RequestLineDefinition requestLine = mock(RequestLineDefinition.class);
        when(requestLine.pathArgs()).thenReturn(ImmutableSet.of("pathArg"));

        EndpointDefinition endpoint = mock(EndpointDefinition.class);
        when(endpoint.argsWithAutoDefined()).thenReturn(Optional.of(ImmutableMap.of()));
        when(endpoint.http()).thenReturn(requestLine);

        EndpointDefinitionValidator.PATH_PARAM.validate(endpoint);
    }

    @Test
    public void testNoGetBodyValidator() throws Exception {
        ArgumentDefinition bodyArg = ArgumentDefinition.builder()
                .type(ConjureType.fromString("string"))
                .paramType(ArgumentDefinition.ParamType.BODY)
                .build();
        RequestLineDefinition requestLine = RequestLineDefinition.of(HttpMethod.GET, "/a/path");
        EndpointDefinition.Builder endpoint = EndpointDefinition.builder()
                .args(ImmutableMap.of("bodyArg", bodyArg))
                .http(requestLine);
        Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> endpoint.build())
                .withMessage("Endpoint cannot be a GET and contain a body: " + requestLine);
    }
}

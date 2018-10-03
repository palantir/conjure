/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.conjure.defs.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.BodyParameterType;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PathParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import org.junit.Test;

public final class EndpointDefinitionTest {

    private final DealiasingTypeVisitor emptyDealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of());

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

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Non body parameters cannot be of the 'binary' type: 'testArg' is not allowed");
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
        EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor);
    }

    @Test
    public void testSingleBodyParamValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg1")).build())
                .args(BODY_ARG_BUILDER.argName(ArgumentName.of("bodyArg2")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint cannot have multiple body parameters: [bodyArg1, bodyArg2]");
    }

    @Test
    public void testPathParamValidatorUniquePathParams() {
        ArgumentDefinition paramDefinition1 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(Type.primitive(PrimitiveType.STRING))
                .paramType(ParameterType.path(PathParameterType.of()))
                .build();
        ArgumentDefinition paramDefinition2 = ArgumentDefinition.builder()
                .argName(ArgumentName.of("paramName"))
                .type(Type.primitive(PrimitiveType.STRING))
                .paramType(ParameterType.path(PathParameterType.of()))
                .build();


        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(paramDefinition1, paramDefinition2))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Path parameter with identifier \"paramName\" is defined multiple times for endpoint");
    }

    @Test
    public void testPathParamValidatorExtraParams() {
        ArgumentDefinition paramDefinition = ArgumentDefinition.builder()
                .type(Type.primitive(PrimitiveType.STRING))
                .argName(ArgumentName.of("paramName"))
                .paramType(ParameterType.path(PathParameterType.of()))
                .build();

        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(paramDefinition)
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "Path parameters defined in endpoint but not present in path template: [paramName]");
    }

    @Test
    public void testPathParamValidatorMissingParams() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path/{paramName}"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
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

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
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
                        .type(Type.list(ListType.builder().itemType(Type.primitive(PrimitiveType.STRING)).build()))
                        .paramType(ParameterType.header(HeaderParameterType.of(ParameterId.of("someId"))))
                        .build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Header parameters must be enums, primitives, aliases or optional primitive:"
                        + " \"someName\" is not allowed");
    }

    @Test
    public void testComplexHeaderObject() {
        TypeName typeName = TypeName.of("SomeObject", "com.palantir.foo");
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("someName"))
                        .type(Type.reference(typeName))
                        .paramType(ParameterType.header(HeaderParameterType.of(ParameterId.of("SomeId"))))
                        .build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of(
                typeName, TypeDefinition.object(ObjectDefinition.of(typeName, ImmutableList.of(), Documentation.of("")))
        ));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), dealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Header parameters must be enums, primitives, aliases or optional primitive:"
                        + " \"someName\" is not allowed");
    }
}

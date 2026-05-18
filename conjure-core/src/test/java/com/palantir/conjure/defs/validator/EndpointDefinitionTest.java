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
import com.palantir.conjure.spec.AliasDefinition;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.BodyParameterType;
import com.palantir.conjure.spec.Documentation;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointError;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.EnumDefinition;
import com.palantir.conjure.spec.EnumValueDefinition;
import com.palantir.conjure.spec.ErrorNamespace;
import com.palantir.conjure.spec.ErrorTypeName;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ListType;
import com.palantir.conjure.spec.ObjectDefinition;
import com.palantir.conjure.spec.OptionalType;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PathParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.QueryParameterType;
import com.palantir.conjure.spec.SetType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.spec.TypeDefinition;
import com.palantir.conjure.spec.TypeName;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public final class EndpointDefinitionTest {

    private final DealiasingTypeVisitor emptyDealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of());

    private static final EndpointName ENDPOINT_NAME = EndpointName.of("test");

    private static ArgumentDefinition.Builder bodyArgBuilder() {
        return ArgumentDefinition.builder()
                .type(Type.primitive(PrimitiveType.ANY))
                .paramType(ParameterType.body(BodyParameterType.of()));
    }

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
                .hasMessage("Non body parameters cannot contain the 'binary' type. Parameter 'testArg' "
                        + "from endpoint 'test{http: GET /a/path}' violates this constraint.");
    }

    @Test
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
                .args(bodyArgBuilder().argName(ArgumentName.of("bodyArg1")).build())
                .args(bodyArgBuilder().argName(ArgumentName.of("bodyArg2")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint 'test{http: GET /a/path}' cannot have multiple body parameters: "
                        + "[bodyArg1, bodyArg2]");
    }

    @Test
    public void testNoOptionalBinaryBodyParamValidator_direct() {
        EndpointDefinition definition = EndpointDefinition.builder()
                .args(bodyArgBuilder()
                        .argName(ArgumentName.of("bodyArg1"))
                        .type(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.BINARY))))
                        .build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"))
                .build();

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition, emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint BODY argument must not be optional<binary> or alias thereof: "
                        + "test{http: POST /a/path}");
    }

    @Test
    public void testNoOptionalBinaryBodyParamValidator_throughAlias() {
        TypeName typeName = TypeName.of("OptionalBinary", "foo");
        EndpointDefinition definition = EndpointDefinition.builder()
                .args(bodyArgBuilder()
                        .argName(ArgumentName.of("someName"))
                        .type(Type.reference(typeName))
                        .build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"))
                .build();

        DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of(
                typeName,
                TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(typeName)
                        .alias(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.BINARY))))
                        .docs(Documentation.of(""))
                        .build())));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition, dealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint BODY argument must not be optional<binary> or alias thereof: "
                        + "test{http: POST /a/path}");
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
                .hasMessage("Path parameter with identifier \"paramName\" is "
                        + "defined multiple times for endpoint test{http: GET /a/path}");
    }

    @Test
    public void testQueryParamValidatorBinaryParamInContainer_list() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.list(ListType.of(Type.primitive(PrimitiveType.BINARY))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Non body parameters cannot contain the 'binary' type. Parameter 'paramName' "
                        + "from endpoint 'test{http: GET /path}' violates this constraint.");
    }

    @Test
    public void testQueryParamValidatorBearerTokenParamInContainer() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.list(ListType.of(Type.primitive(PrimitiveType.BEARERTOKEN))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Path or query parameters of type 'bearertoken' are not allowed");
    }

    @Test
    public void testQueryParamValidatorBinaryParamInContainer_set() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.set(SetType.of(Type.primitive(PrimitiveType.BINARY))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Non body parameters cannot contain the 'binary' type. Parameter 'paramName' "
                        + "from endpoint 'test{http: GET /path}' violates this constraint.");
    }

    @Test
    public void testQueryParamValidatorBinaryParamInContainer_optional() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.BINARY))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Non body parameters cannot contain the 'binary' type. Parameter 'paramName' "
                        + "from endpoint 'test{http: GET /path}' violates this constraint.");
    }

    @Test
    public void testQueryParamValidatorNestedContainer_list_optional() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.list(
                                ListType.of(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.STRING))))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Query parameters must be enums or primitives when de-aliased, or containers of these (list,"
                                + " sets, optionals): 'paramName' is not allowed on endpoint 'test{http: GET /path}'");
    }

    @Test
    public void testQueryParamValidatorNestedContainer_optional_list() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.optional(
                                OptionalType.of(Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Query parameters must be enums or primitives when de-aliased, or containers of these (list,"
                                + " sets, optionals): 'paramName' is not allowed on endpoint 'test{http: GET /path}'");
    }

    @Test
    public void testQueryParamValidatorNestedContainer_list_set() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.list(ListType.of(Type.set(SetType.of(Type.primitive(PrimitiveType.STRING))))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Query parameters must be enums or primitives when de-aliased, or containers of these (list,"
                                + " sets, optionals): 'paramName' is not allowed on endpoint 'test{http: GET /path}'");
    }

    @Test
    public void testQueryParamValidatorNestedContainer_list_aliased_optional() {
        TypeName typeName = TypeName.of("OptionalAlias", "com.palantir.foo");
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.list(ListType.of(Type.reference(typeName))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of(
                typeName,
                TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(typeName)
                        .alias(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.STRING))))
                        .build())));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), dealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Query parameters must be enums or primitives when de-aliased, or containers of these (list,"
                                + " sets, optionals): 'paramName' is not allowed on endpoint 'test{http: GET /path}'");
    }

    @Test
    public void testQueryParamValidatorNestedContainer_aliased_list_optional() {
        TypeName typeName = TypeName.of("ListOptionalAlias", "com.palantir.foo");
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("paramName"))
                        .type(Type.reference(typeName))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of(
                typeName,
                TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(typeName)
                        .alias(Type.list(
                                ListType.of(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.STRING))))))
                        .build())));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), dealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Query parameters must be enums or primitives when de-aliased, or containers of these (list,"
                                + " sets, optionals): 'paramName' is not allowed on endpoint 'test{http: GET /path}'");
    }

    @Test
    public void testQueryParamValidatorContainerIsValid() {
        TypeName typeName = TypeName.of("OptionalAlias", "com.palantir.foo");
        TypeName enumTypeName = TypeName.of("ExampleEnum", "com.palantir.foo");
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("aliasParam"))
                        .type(Type.reference(typeName))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("optionalParam"))
                        .type(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.STRING))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("listParam"))
                        .type(Type.list(ListType.of(Type.primitive(PrimitiveType.STRING))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("setParam"))
                        .type(Type.set(SetType.of(Type.primitive(PrimitiveType.STRING))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .args(ImmutableList.of(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("listEnum"))
                        .type(Type.list(ListType.of(Type.reference(enumTypeName))))
                        .paramType(ParameterType.query(QueryParameterType.of(ParameterId.of("value"))))
                        .build()))
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/path"));

        DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of(
                typeName,
                TypeDefinition.alias(AliasDefinition.builder()
                        .typeName(typeName)
                        .alias(Type.list(
                                ListType.of(Type.optional(OptionalType.of(Type.primitive(PrimitiveType.STRING))))))
                        .build()),
                enumTypeName,
                TypeDefinition.enum_(EnumDefinition.builder()
                        .typeName(enumTypeName)
                        .values(List.of(
                                EnumValueDefinition.builder().value("FOO").build()))
                        .build())));

        EndpointDefinitionValidator.validateAll(definition.build(), dealiasingVisitor);
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
                .hasMessage("Path parameters [paramName] defined path template but not present in endpoint: "
                        + "test{http: GET /a/path/{paramName}}");
    }

    @Test
    public void testNoGetBodyValidator() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(bodyArgBuilder().argName(ArgumentName.of("bodyArg")).build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Endpoint 'test{http: GET /a/path}' cannot be a GET and contain a body");
    }

    @Test
    public void testComplexHeader() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .args(ArgumentDefinition.builder()
                        .argName(ArgumentName.of("someName"))
                        .type(Type.list(ListType.builder()
                                .itemType(Type.primitive(PrimitiveType.STRING))
                                .build()))
                        .paramType(ParameterType.header(HeaderParameterType.of(ParameterId.of("someId"))))
                        .build())
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/a/path"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Header parameters must be enums, primitives, aliases or optional primitive: "
                        + "\"someName\" is not allowed on endpoint test{http: GET /a/path}");
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
                typeName,
                TypeDefinition.object(ObjectDefinition.of(typeName, ImmutableList.of(), Documentation.of("")))));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), dealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Header parameters must be enums, primitives, aliases or optional primitive: "
                        + "\"someName\" is not allowed on endpoint test{http: GET /a/path}");
    }

    @Test
    public void testNoUnsupportedHttpMethod() {
        EndpointDefinition.Builder definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.valueOf("UNKNOWN"))
                .httpPath(HttpPath.of("/"));

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition.build(), emptyDealiasingVisitor))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("HTTP method must be ("
                        + HttpMethod.values().stream().map(HttpMethod::toString).collect(Collectors.joining("|"))
                        + "), but received 'UNKNOWN' in endpoint 'test{http: UNKNOWN /}'.");
    }

    @Test
    public void testDuplicateEndpointErrorsAreInvalid() {
        ErrorTypeName errorTypeName = ErrorTypeName.builder()
                .name("Error1")
                .package_("test.api")
                .namespace(ErrorNamespace.of("Test"))
                .build();
        EndpointDefinition definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/get"))
                .errors(List.of(
                        EndpointError.builder()
                                .error(errorTypeName)
                                .docs(Documentation.of("docs"))
                                .build(),
                        EndpointError.builder()
                                .error(errorTypeName)
                                .docs(Documentation.of("different docs but same error"))
                                .build()))
                .build();

        assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(definition, emptyDealiasingVisitor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Error 'Error1' with namespace 'Test' is declared multiple times in endpoint 'test{http: GET"
                                + " /get}'");
    }

    @Test
    public void testErrorsAreIdentifiedByNameAndNamespace() {
        ErrorTypeName errorTypeName = ErrorTypeName.of("Error1", "test.api", ErrorNamespace.of("Test"));
        EndpointDefinition definition = EndpointDefinition.builder()
                .endpointName(ENDPOINT_NAME)
                .httpMethod(HttpMethod.GET)
                .httpPath(HttpPath.of("/get"))
                .errors(List.of(
                        EndpointError.builder().error(errorTypeName).build(),
                        EndpointError.builder()
                                .error(ErrorTypeName.builder()
                                        .from(errorTypeName)
                                        .namespace(ErrorNamespace.of("Other"))
                                        .build())
                                .build()))
                .build();

        // Should not throw exception since the errors have different namespaces
        EndpointDefinitionValidator.validateAll(definition, emptyDealiasingVisitor);
    }
}

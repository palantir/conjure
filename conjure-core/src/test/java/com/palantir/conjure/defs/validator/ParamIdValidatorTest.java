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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import com.palantir.conjure.CaseConverter;
import com.palantir.conjure.spec.ArgumentDefinition;
import com.palantir.conjure.spec.ArgumentName;
import com.palantir.conjure.spec.EndpointDefinition;
import com.palantir.conjure.spec.EndpointName;
import com.palantir.conjure.spec.HeaderParameterType;
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ParameterId;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.QueryParameterType;
import com.palantir.conjure.spec.Type;
import com.palantir.conjure.visitor.DealiasingTypeVisitor;
import java.util.List;
import javax.annotation.CheckReturnValue;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ParamIdValidatorTest {
    private static final ArgumentName PARAMETER_NAME = ArgumentName.of("arg");
    private final DealiasingTypeVisitor dealiasingVisitor = new DealiasingTypeVisitor(ImmutableMap.of());

    @Test
    public void testValidNonHeader() {
        for (String paramId : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234", "foo_bar", "foo-bar")) {
            assertThatEndpointValidation(ParameterType.query(QueryParameterType.of(ParameterId.of(paramId))))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testValidHeader() {
        List<String> paramIds = ImmutableList.of(
                HttpHeaders.AUTHORIZATION, HttpHeaders.X_XSS_PROTECTION, HttpHeaders.P3P, HttpHeaders.SET_COOKIE2);
        for (String paramId : paramIds) {
            assertThatEndpointValidation(ParameterType.header(HeaderParameterType.of(ParameterId.of(paramId))))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    public void testInvalidNonHeader() {
        for (String paramId : ImmutableList.of("AB", "123", "foo.bar")) {
            ParameterType parameterType = ParameterType.query(QueryParameterType.of(ParameterId.of(paramId)));
            assertThatThrownByEndpointValidation(parameterType)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(
                            "Query param id %s on endpoint test{http: POST /a/path} must match one of the "
                                    + "following patterns: [LOWER_CAMEL_CASE[%s], KEBAB_CASE[%s], SNAKE_CASE[%s]]",
                            paramId,
                            CaseConverter.CAMEL_CASE_PATTERN.pattern(),
                            CaseConverter.KEBAB_CASE_PATTERN.pattern(),
                            CaseConverter.SNAKE_CASE_PATTERN.pattern());
        }
    }

    @Test
    public void testInvalidHeader() {
        for (String paramId : ImmutableList.of("authorization", "123", "Foo_Bar", "Foo.Bar")) {
            ParameterType parameterType = ParameterType.header(HeaderParameterType.of(ParameterId.of(paramId)));
            assertThatThrownByEndpointValidation(parameterType)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(
                            "Header parameter id %s on endpoint test{http: POST /a/path} must match pattern %s",
                            paramId, EndpointDefinitionValidator.HEADER_PATTERN);
        }
    }

    @Test
    public void testProtocolHeaders() {
        for (String protocolHeader : EndpointDefinitionValidator.PROTOCOL_HEADERS) {
            ParameterType parameterType = ParameterType.header(HeaderParameterType.of(ParameterId.of(protocolHeader)));
            assertThatThrownByEndpointValidation(parameterType)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage(
                            "Header parameter id %s on endpoint test{http: POST /a/path} should not be one of the "
                                    + "protocol headers %s",
                            protocolHeader, EndpointDefinitionValidator.PROTOCOL_HEADERS);
        }
    }

    @CheckReturnValue
    private AbstractThrowableAssert<?, ? extends Throwable> assertThatThrownByEndpointValidation(
            ParameterType paramType) {
        return assertThatEndpointValidation(paramType);
    }

    @CheckReturnValue
    private AbstractThrowableAssert<?, ? extends Throwable> assertThatEndpointValidation(ParameterType paramType) {
        return assertThat(Assertions.catchThrowable(() -> {
            ArgumentDefinition arg = ArgumentDefinition.builder()
                    .argName(PARAMETER_NAME)
                    .paramType(paramType)
                    .type(Type.primitive(PrimitiveType.INTEGER))
                    .build();
            EndpointDefinition definition = EndpointDefinition.builder()
                    .httpMethod(HttpMethod.POST)
                    .httpPath(HttpPath.of("/a/path"))
                    .args(arg)
                    .endpointName(EndpointName.of("test"))
                    .build();

            EndpointDefinitionValidator.validateAll(definition, dealiasingVisitor);
        }));
    }
}

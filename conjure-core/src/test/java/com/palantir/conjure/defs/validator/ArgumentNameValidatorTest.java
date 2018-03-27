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
import com.palantir.conjure.spec.HttpMethod;
import com.palantir.conjure.spec.HttpPath;
import com.palantir.conjure.spec.ParameterType;
import com.palantir.conjure.spec.PrimitiveType;
import com.palantir.conjure.spec.Type;
import org.junit.Test;

public final class ArgumentNameValidatorTest {

    @Test
    @SuppressWarnings("CheckReturnValue")
    public void testValid() {
        for (String paramName : ImmutableList.of("f", "foo", "fooBar", "fooBar1", "a1Foo234")) {
            EndpointDefinition.Builder endpoint = createEndpoint(paramName);
            // Passes validation
            endpoint.build();
        }
    }

    @Test
    public void testInvalid() {
        for (String paramName : ImmutableList.of("AB", "123", "foo_bar", "foo-bar", "foo.bar")) {
            EndpointDefinition.Builder endpoint = createEndpoint(paramName);
            assertThatThrownBy(() -> EndpointDefinitionValidator.validateAll(endpoint.build()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Parameter names in endpoint paths and service definitions must match pattern %s: %s",
                            EndpointDefinitionValidator.ANCHORED_PATTERN,
                            paramName);
        }
    }

    private EndpointDefinition.Builder createEndpoint(String paramName) {
        ArgumentDefinition arg = ArgumentDefinition.builder()
                .paramType(ParameterType.body(BodyParameterType.of()))
                .type(Type.primitive(PrimitiveType.STRING))
                .argName(ArgumentName.of(paramName))
                .build();
        return EndpointDefinition.builder()
                .httpMethod(HttpMethod.POST)
                .httpPath(HttpPath.of("/a/path"))
                .args(ImmutableList.of(arg))
                .endpointName(EndpointName.of("test"));
    }
}

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

package com.palantir.conjure.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Test;

public final class KebabCaseEnforcingAnnotationInspectorTest {

    private static final ObjectMapper mapper = ConjureParser.createConjureParserObjectMapper();

    static final class ValidTarget {
        String fooBar;

        @JsonProperty("foo-bar")
        ValidTarget setFooBar(String fooBar) {
            this.fooBar = fooBar;
            return this;
        }
    }

    @Test
    public void testValidDefinitionsCarryJsonPropertyAnnotationsOnSetters() throws Exception {
        assertThat(mapper.readValue("{\"foo-bar\": \"baz\"}", ValidTarget.class))
                .isEqualToComparingFieldByField(new ValidTarget().setFooBar("baz"));
    }

    static final class NoAnnotationInvalidTarget {
        String fooBar;

        void setFooBar(String fooBar) {
            this.fooBar = fooBar;
        }
    }

    @Test
    public void testSetterWithoutAnnotationIsInvalid() throws Exception {
        assertThatThrownBy(() -> mapper.readValue("{\"fooBar\": \"baz\"}", NoAnnotationInvalidTarget.class))
                .isInstanceOf(InvalidDefinitionException.class)
                .hasMessageContaining("All setter ({@code set*}) deserialization targets require "
                        + "@JsonProperty annotations: setFooBar");
    }

    static final class NonKebabCaseAnnotationInvalidTarget {
        String fooBar;

        @JsonProperty("fooBar")
        void setFooBar(String fooBar) {
            this.fooBar = fooBar;
        }
    }

    @Test
    public void testSetterWithNonKebabCaseAnnotationIsInvalid() throws Exception {
        assertThatThrownBy(() -> mapper.readValue("{\"fooBar\": \"baz\"}", NonKebabCaseAnnotationInvalidTarget.class))
                .isInstanceOf(InvalidDefinitionException.class)
                .hasMessageContaining("Conjure grammar requires kebab-case field names: fooBar");
    }
}

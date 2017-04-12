/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public final class KebabCaseEnforcingAnnotationInspectorTest {

    private static final ObjectMapper mapper = Conjure.createConjureParserObjectMapper();

    private static class ValidTarget {
        String fooBar;

        @JsonProperty("foo-bar")
        ValidTarget setFooBar(String fooBar) {
            this.fooBar = fooBar;
            return this;
        }
    }

    @Test
    public void testValidDefinitionsCarryJsonPropertyAnnotationsOnSetters() throws Exception {
        assertThat(
                mapper.readValue("{\"foo-bar\": \"baz\"}", ValidTarget.class))
                .isEqualToComparingFieldByField(new ValidTarget().setFooBar("baz"));
    }

    private static class NoAnnotationInvalidTarget {
        String fooBar;

        void setFooBar(String fooBar) {
            this.fooBar = fooBar;
        }
    }

    @Test
    public void testSetterWithoutAnnotationIsInvalid() throws Exception {
        assertThatThrownBy(() -> mapper.readValue("{\"fooBar\": \"baz\"}", NoAnnotationInvalidTarget.class))
                .isInstanceOf(JsonMappingException.class)
                .hasCause(new IllegalArgumentException("All setter ({@code set*}) deserialization targets require "
                        + "@JsonProperty annotations: setFooBar"));
    }

    private static class NonKebabCaseAnnotationInvalidTarget {
        String fooBar;

        @JsonProperty("fooBar")
        void setFooBar(String fooBar) {
            this.fooBar = fooBar;
        }
    }

    @Test
    public void testSetterWithNonKebabCaseAnnotationIsInvalid() throws Exception {
        assertThatThrownBy(() -> mapper.readValue("{\"fooBar\": \"baz\"}", NonKebabCaseAnnotationInvalidTarget.class))
                .isInstanceOf(JsonMappingException.class)
                .hasCause(new IllegalArgumentException("Conjure grammar requires kebab-case field names: fooBar"));
    }
}

/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.remoting2.ext.jackson.ObjectMappers;
import java.nio.ByteBuffer;
import org.junit.Test;
import test.api.BinaryExample;
import test.api.DoubleAliasExample;
import test.api.EnumExample;
import test.api.IntegerAliasExample;
import test.api.ListExample;
import test.api.MapExample;
import test.api.OptionalExample;
import test.api.SetExample;
import test.api.StringAliasExample;
import test.api.StringExample;

public final class WireFormatTests {

    private final ObjectMapper mapper = ObjectMappers.guavaJdk7Jdk8();

    @Test
    public void testMissingCollectionFieldsDeserializeAsEmpty() throws Exception {
        assertThat(mapper.readValue("{}", SetExample.class).getItems()).isEmpty();
        assertThat(mapper.readValue("{}", ListExample.class).getItems()).isEmpty();
        assertThat(mapper.readValue("{}", MapExample.class).getItems()).isEmpty();
    }

    @Test
    public void testMissingOptionalFieldsDeserializeAsEmpty() throws Exception {
        assertThat(mapper.readValue("{}", OptionalExample.class).getItem().isPresent()).isFalse();
    }

    @Test
    public void testPresentCollectionFieldsDeserializeWithElements() throws Exception {
        assertThat(mapper.readValue("{\"items\": [\"a\", \"b\"]}", SetExample.class).getItems()).contains("a", "b");
        assertThat(mapper.readValue("{\"items\": [\"a\", \"b\"]}", ListExample.class).getItems()).contains("a", "b");
        assertThat(mapper.readValue("{\"items\": {\"a\": \"b\"}}", MapExample.class).getItems())
                .containsEntry("a", "b");
    }

    @Test
    public void testPresentOptionalDeserializesWithElement() throws Exception {
        assertThat(mapper.readValue("{\"item\": \"a\"}", OptionalExample.class).getItem()).contains("a");
    }

    @Test
    public void testPrimitivesMayNotBeNull() throws Exception {
        assertThatThrownBy(() -> mapper.readValue("{}", StringExample.class))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Some required fields have not been set: [string]");

        // TODO(melliot): integer and double primitives must also throw (#245)
    }

    @Test
    public void testBinaryFieldsDeserializeFromBase64() throws Exception {
        assertThat(mapper.readValue("{\"binary\": \"AAEC\"}", BinaryExample.class).getBinary())
                .isEqualTo(ByteBuffer.wrap(new byte[]{0, 1, 2}));
    }

    @Test
    public void testBinaryFieldsSerializeToBase64() throws Exception {
        assertThat(mapper.writeValueAsString(
                    BinaryExample.builder().binary(ByteBuffer.wrap(new byte[]{0, 1, 2})).build()))
                .isEqualTo("{\"binary\":\"AAEC\"}");
    }

    @Test
    public void testObjectsThatImplementEqualityImplementDeepEquality() throws Exception {
        assertThat(SetExample.builder().items("a").items("b").build())
                .isEqualTo(SetExample.builder().items("b").items("a").build());
        assertThat(SetExample.builder().items("a").items("c").build())
                .isNotEqualTo(SetExample.builder().items("b").items("a").build());

        assertThat(OptionalExample.builder().item("a").build())
                .isEqualTo(OptionalExample.builder().item("a").build());
        assertThat(OptionalExample.builder().build())
                .isEqualTo(OptionalExample.builder().build());
        assertThat(OptionalExample.builder().item("a").build())
                .isNotEqualTo(OptionalExample.builder().item("b").build());
    }

    @Test
    public void testObjectsThatImplementHashCodeImplementDeepHashCode() {
        assertThat(BinaryExample.of(ByteBuffer.wrap(new byte[]{0, 1, 2})).hashCode())
                .isEqualTo(BinaryExample.of(ByteBuffer.wrap(new byte[]{0, 1, 2})).hashCode());

        assertThat(OptionalExample.builder().item("a").build().hashCode())
                .isEqualTo(OptionalExample.builder().item("a").build().hashCode());

        assertThat(SetExample.builder().items("a").items("b").build().hashCode())
                .isEqualTo(SetExample.builder().items("b").items("a").build().hashCode());
    }

    @Test
    public void testEnumCasingDeserializationInvariantToInputCase() throws Exception {
        assertThat(mapper.readValue("\"ONE\"", EnumExample.class)).isEqualTo(EnumExample.ONE);
        assertThat(mapper.readValue("\"one\"", EnumExample.class)).isEqualTo(EnumExample.ONE);
        assertThat(mapper.readValue("\"onE\"", EnumExample.class)).isEqualTo(EnumExample.ONE);
        assertThat(mapper.readValue("\"oNE\"", EnumExample.class)).isEqualTo(EnumExample.ONE);
    }

    @Test
    public void testIgnoreUnknownValuesDuringDeserialization() throws Exception {
        assertThat(mapper.readValue("{\"fake\": \"fake\"}", OptionalExample.class))
                .isEqualTo(OptionalExample.builder().build());
    }

    @Test
    public void testEnumRoundTripsUnknownValue() throws Exception {
        assertThat(mapper.writeValueAsString(mapper.readValue("\"FAKE_FAKE_FAKE\"", EnumExample.class)))
            .isEqualTo("\"FAKE_FAKE_FAKE\"");
        // nb: we upper-case incoming values to sanitize, so fake_FAKE_fake would fail here
    }

    @Test
    public void testAliasTypesEqualWhenInnerTypeEqual() throws Exception {
        assertThat(StringAliasExample.of("a")).isEqualTo(StringAliasExample.of("a"));
        assertThat(IntegerAliasExample.of(103)).isEqualTo(IntegerAliasExample.of(103));
        assertThat(DoubleAliasExample.of(10.3)).isEqualTo(DoubleAliasExample.of(10.3));
    }

    @Test
    public void testAliasTypesHashCodeEqualWhenInnerTypeEqual() throws Exception {
        assertThat(StringAliasExample.of("a").hashCode()).isEqualTo(StringAliasExample.of("a").hashCode());
        assertThat(IntegerAliasExample.of(103).hashCode()).isEqualTo(IntegerAliasExample.of(103).hashCode());
        assertThat(DoubleAliasExample.of(10.3).hashCode()).isEqualTo(DoubleAliasExample.of(10.3).hashCode());
    }

}

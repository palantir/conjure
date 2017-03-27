/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.palantir.remoting2.ext.jackson.ObjectMappers;
import java.nio.ByteBuffer;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import test.api.UnionTypeExample;

public final class WireFormatTests {

    private final ObjectMapper mapper = ObjectMappers.guavaJdk7Jdk8();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

    @Test
    public void testUnionType() throws Exception {
        StringExample stringExample = StringExample.of("foo");
        UnionTypeExample unionTypeStringExample = UnionTypeExample.of(stringExample);
        UnionTypeExample unionTypeSet = UnionTypeExample.of(ImmutableSet.of("item"));
        UnionTypeExample unionTypeInt = UnionTypeExample.of(5);
        String serializedUnionTypeStringExample = "{\"type\":\"StringExample\",\"stringExample\":{\"string\":\"foo\"}}";
        String serializedUnionTypeSet = "{\"type\":\"set<string>\",\"set<string>\":[\"item\"]}";
        String serializedUnionTypeInt = "{\"type\":\"integer\",\"integer\":5}";

        // serialization
        assertThat(mapper.writeValueAsString(unionTypeStringExample)).isEqualTo(serializedUnionTypeStringExample);
        assertThat(mapper.writeValueAsString(unionTypeSet)).isEqualTo(serializedUnionTypeSet);
        assertThat(mapper.writeValueAsString(unionTypeInt)).isEqualTo(serializedUnionTypeInt);
        // serialization of member type is unchanged
        assertThat(mapper.writeValueAsString(stringExample)).isEqualTo("{\"string\":\"foo\"}");

        // deserialization and equals()
        assertThat(mapper.readValue(serializedUnionTypeStringExample, UnionTypeExample.class))
                .isEqualTo(unionTypeStringExample);
        assertThat(mapper.readValue(serializedUnionTypeSet, UnionTypeExample.class)).isEqualTo(unionTypeSet);
        assertThat(mapper.readValue(serializedUnionTypeInt, UnionTypeExample.class)).isEqualTo(unionTypeInt);

        assertThat(unionTypeStringExample).isEqualTo(stringExample);
        assertThat(unionTypeSet).isEqualTo(ImmutableSet.of("item"));
        assertThat(unionTypeInt).isEqualTo(5);

        // visitor
        UnionTypeExample.Visitor<Integer> visitor = new TestVisitor();
        assertThat(unionTypeStringExample.accept(visitor)).isEqualTo("foo".length());
        assertThat(unionTypeInt.accept(visitor)).isEqualTo(5);
        assertThat(unionTypeSet.accept(visitor)).isEqualTo(1);
    }

    @Test
    public void testUnionType_unknownType() throws Exception {
        String serializedUnionTypeUnknown = "{\"type\":\"unknown\",\"value\":5}";
        UnionTypeExample unionTypeUnknown = mapper.readValue(serializedUnionTypeUnknown, UnionTypeExample.class);
        assertThat(mapper.writeValueAsString(unionTypeUnknown)).isEqualTo(serializedUnionTypeUnknown);
        assertThat(unionTypeUnknown.accept(new TestVisitor())).isEqualTo(0);
    }

    @Test
    public void testUnionType_noType() throws Exception {
        String noType = "{\"typ\":\"unknown\",\"value\":5}";
        expectedException.expect(JsonMappingException.class);
        mapper.readValue(noType, UnionTypeExample.class);
    }

    private static class TestVisitor implements UnionTypeExample.Visitor<Integer> {

        @Override
        public Integer visit(StringExample stringExampleValue) {
            return stringExampleValue.getString().length();
        }

        @Override
        public Integer visit(Set<String> setStringValue) {
            return setStringValue.size();
        }

        @Override
        public Integer visit(int integerValue) {
            return integerValue;
        }

        @Override
        public Integer visitUnknown(String unknownType) {
            return 0;
        }

    }

}

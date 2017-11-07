/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.palantir.remoting3.ext.jackson.ObjectMappers;
import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.api.BinaryExample;
import test.api.DateTimeExample;
import test.api.DoubleAliasExample;
import test.api.EmptyObjectExample;
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

    private final ObjectMapper mapper = ObjectMappers.newServerObjectMapper();

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
                .isEqualTo(ByteBuffer.wrap(new byte[] {0, 1, 2}));
    }

    @Test
    public void testBinaryFieldsSerializeToBase64() throws Exception {
        assertThat(mapper.writeValueAsString(
                BinaryExample.builder().binary(ByteBuffer.wrap(new byte[] {0, 1, 2})).build()))
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
        assertThat(BinaryExample.of(ByteBuffer.wrap(new byte[] {0, 1, 2})).hashCode())
                .isEqualTo(BinaryExample.of(ByteBuffer.wrap(new byte[] {0, 1, 2})).hashCode());

        assertThat(OptionalExample.builder().item("a").build().hashCode())
                .isEqualTo(OptionalExample.builder().item("a").build().hashCode());

        assertThat(SetExample.builder().items("a").items("b").build().hashCode())
                .isEqualTo(SetExample.builder().items("b").items("a").build().hashCode());
    }

    @Test
    public void testEmptyObjectsSerialize() throws Exception {
        assertThat(mapper.writeValueAsString(EmptyObjectExample.of())).isEqualTo("{}");
    }

    @Test
    public void testEmptyObjectsDeserialize() throws Exception {
        assertThat(mapper.readValue("{}", EmptyObjectExample.class)).isEqualTo(EmptyObjectExample.of());
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
        UnionTypeExample unionTypeStringExample = UnionTypeExample.stringExample(stringExample);
        UnionTypeExample unionTypeSet = UnionTypeExample.set(ImmutableSet.of("item"));
        UnionTypeExample unionTypeInt = UnionTypeExample.thisFieldIsAnInteger(5);
        String serializedUnionTypeStringExample = "{\"type\":\"stringExample\",\"stringExample\":{\"string\":\"foo\"}}";
        String serializedUnionTypeSet = "{\"type\":\"set\",\"set\":[\"item\"]}";
        String serializedUnionTypeInt = "{\"type\":\"thisFieldIsAnInteger\",\"thisFieldIsAnInteger\":5}";

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

    @Test
    public void testDateTime_roundTrip() throws Exception {
        String serialized = "{\"datetime\":\"2017-01-02T03:04:05.000000006Z\"}";
        DateTimeExample deserialized = DateTimeExample.of(
                ZonedDateTime.of(2017, 1, 2, 3, 4, 5, 6, ZoneId.of("UTC")));
        assertThat(mapper.writeValueAsString(deserialized)).isEqualTo(serialized);
        assertThat(mapper.readValue(serialized, DateTimeExample.class)).isEqualTo(deserialized);
    }

    @Test
    public void testDateTimeType_acceptFormats() throws Exception {
        DateTimeExample reference = DateTimeExample.of(ZonedDateTime.parse("2017-01-02T03:04:05.000000006Z"));

        assertThat(mapper.readValue("{\"datetime\":\"2017-01-02T03:04:05.000000006Z\"}", DateTimeExample.class))
                .isEqualTo(reference);

        assertThat(mapper.readValue("{\"datetime\":\"2017-01-02T03:04:05.000000006+00:00\"}", DateTimeExample.class))
                .isEqualTo(reference);

        assertThat(mapper.readValue("{\"datetime\":\"2017-01-02T04:04:05.000000006+01:00\"}", DateTimeExample.class))
                .isEqualTo(reference);

        assertThat(mapper.readValue(
                "{\"datetime\":\"2017-01-02T04:04:05.000000006+01:00[Europe/Berlin]\"}", DateTimeExample.class))
                .isEqualTo(reference);

        DateTimeExample secondsOnly = DateTimeExample.of(ZonedDateTime.parse("2017-01-02T03:04:05.000000000Z"));

        // seconds
        assertThat(mapper.readValue("{\"datetime\":\"2017-01-02T03:04:05Z\"}", DateTimeExample.class))
                .isEqualTo(secondsOnly);

        // milli
        assertThat(mapper.readValue("{\"datetime\":\"2017-01-02T03:04:05.000Z\"}", DateTimeExample.class))
                .isEqualTo(secondsOnly);

        // micro
        assertThat(mapper.readValue("{\"datetime\":\"2017-01-02T03:04:05.000000Z\"}", DateTimeExample.class))
                .isEqualTo(secondsOnly);
    }

    @Test
    public void testDateTimeType_equality() throws Exception {
        ZonedDateTime aa = ZonedDateTime.parse("2017-01-02T03:04:05.000000006Z");
        ZonedDateTime bb = ZonedDateTime.parse("2017-01-02T03:04:05.000000006+00:00");
        ZonedDateTime cc = ZonedDateTime.parse("2017-01-02T04:04:05.000000006+01:00");
        ZonedDateTime dd = ZonedDateTime.parse("2017-01-02T04:04:05.000000006+01:00[Europe/Berlin]");

        assertThat(aa.isEqual(bb)).isTrue();
        assertThat(aa.isEqual(cc)).isTrue();
        assertThat(aa.isEqual(dd)).isTrue();
        assertThat(bb.isEqual(cc)).isTrue();
        assertThat(bb.isEqual(dd)).isTrue();
        assertThat(cc.isEqual(dd)).isTrue();

        DateTimeExample one = DateTimeExample.of(ZonedDateTime.parse("2017-01-02T03:04:05.000000006Z"));
        DateTimeExample two = DateTimeExample.of(ZonedDateTime.parse("2017-01-02T04:04:05.000000006+01:00"));
        DateTimeExample three = DateTimeExample.of(
                ZonedDateTime.parse("2017-01-02T04:04:05.000000006+01:00[Europe/Berlin]"));

        assertThat(one).isEqualTo(two);
        assertThat(one).isEqualTo(three);
        assertThat(two).isEqualTo(three);

        assertThat(one.hashCode()).isEqualTo(two.hashCode());
        assertThat(one.hashCode()).isEqualTo(three.hashCode());
        assertThat(two.hashCode()).isEqualTo(three.hashCode());
    }

    private static class TestVisitor implements UnionTypeExample.Visitor<Integer> {
        @Override
        public Integer visitStringExample(StringExample stringExampleValue) {
            return stringExampleValue.getString().length();
        }

        @Override
        public Integer visitSet(Set<String> setStringValue) {
            return setStringValue.size();
        }

        @Override
        public Integer visitAlsoAnInteger(int value) {
            return value;
        }

        @Override
        public Integer visitThisFieldIsAnInteger(int value) {
            return value;
        }

        @Override
        public Integer visitUnknown(String unknownType) {
            return 0;
        }

        @Override
        public Integer visitNew(int value) {
            return value;
        }

        @Override
        public Integer visitInterface(int value) {
            return value;
        }

        @Override
        public Integer visitIf(int value) {
            return value;
        }

    }

}

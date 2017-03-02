/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class SafeLongTests {

    private static final long maxValue = 9007199254740991L;
    private static final long minValue = -9007199254740991L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testTooLarge() {
        long tooLarge = maxValue + 1;

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("number must be safely representable in javascript");
        SafeLong.of(tooLarge);
    }

    @Test
    public void testTooSmall() {
        long tooSmall = minValue - 1;

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("number must be safely representable in javascript");
        SafeLong.of(tooSmall);
    }

    @Test
    public void testOk() {
        SafeLong.of(maxValue);
        SafeLong.of(minValue);
    }

    @Test
    public void testSerde() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SafeLong maxSafeLong = SafeLong.of(maxValue);
        String serialized = mapper.writeValueAsString(maxSafeLong);
        SafeLong deserialized = mapper.readValue(serialized, SafeLong.class);
        assertThat(deserialized).isEqualTo(maxSafeLong);
    }

    @Test
    public void testDeserializationFailsWhenTooLarge() throws JsonParseException, JsonMappingException, IOException {
        String json = "{\"value\": 9007199254740992}";
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, SafeLong>> typeReference = new TypeReference<Map<String, SafeLong>>() {};

        expectedException.expect(JsonMappingException.class);
        mapper.readValue(json, typeReference);
    }
}

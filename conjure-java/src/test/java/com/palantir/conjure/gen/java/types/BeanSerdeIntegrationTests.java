/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.remoting2.ext.jackson.ObjectMappers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import test.api.BearerTokenExample;
import test.api.BooleanExample;
import test.api.ListExample;
import test.api.MapExample;
import test.api.RidExample;
import test.api.SafeLongExample;
import test.api.SetExample;

public final class BeanSerdeIntegrationTests {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final ObjectMapper mapper = ObjectMappers.guavaJdk7Jdk8();

    @Test
    public void testSetExampleSerde() throws Exception {
        testSerde("{\"items\": [\"one\", \"two\"]}", SetExample.class);
    }

    @Test
    public void testListExampleSerde() throws Exception {
        testSerde("{\"items\": [\"one\", \"two\"]}", ListExample.class);
    }

    @Test
    public void testMapExampleSerde() throws Exception {
        testSerde("{\"items\": {\"one\": \"eins\", \"two\": \"二\"}}", MapExample.class);
    }

    @Test
    public void testSafeLongExampleSerde() throws Exception {
        testSerde("{\"safeLongValue\": 9007199254740991}", SafeLongExample.class);
    }

    @Test
    public void testRidExampleSerde() throws Exception {
        testSerde("{\"ridValue\": \"ri.foundry.main.dataset.0\"}", RidExample.class);
    }

    @Test
    public void testBearerTokenExampleSerde() throws Exception {
        testSerde("{\"bearerTokenValue\": \"anything\"}", BearerTokenExample.class);
    }

    @Test
    public void testSafeLongExampleSerde_tooLarge() throws Exception {
        expectedException.expect(JsonMappingException.class);
        expectedException.expectMessage("number must be safely representable in javascript");
        testSerde("{\"safeLongValue\": 9007199254740992}", SafeLongExample.class);
    }

    @Test
    public void testIgnoreProperties() throws Exception {
        // Important for ensuring additive changes don't affect clients adversely
        BooleanExample boolExample = mapper.readValue("{\"coin\": true, \"ignored\": \"field\"}", BooleanExample.class);
        assertThat(boolExample.getCoin()).isEqualTo(true);
    }

    private static <T> void testSerde(String json, Class<T> clazz) throws Exception {
        T example = mapper.readValue(json, clazz);
        String serialized = mapper.writeValueAsString(example);
        T deserialized = mapper.readValue(serialized, clazz);

        assertThat(deserialized).isEqualTo(example);
    }

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.remoting2.ext.jackson.ObjectMappers;
import org.junit.Test;
import test.api.ListExample;
import test.api.MapExample;
import test.api.SetExample;

public final class BeanSerdeIntegrationTests {

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

    private static <T> void testSerde(String json, Class<T> clazz) throws Exception {
        T example = mapper.readValue(json, clazz);
        String serialized = mapper.writeValueAsString(example);
        T deserialized = mapper.readValue(serialized, clazz);

        assertThat(deserialized).isEqualTo(example);
    }

}

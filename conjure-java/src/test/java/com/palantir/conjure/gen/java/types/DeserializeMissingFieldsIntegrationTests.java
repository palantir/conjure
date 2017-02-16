/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.conjure.gen.java.types;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.palantir.remoting2.ext.jackson.ObjectMappers;
import org.junit.Test;
import test.api.ListExample;
import test.api.MapExample;
import test.api.OptionalExample;
import test.api.SetExample;

public final class DeserializeMissingFieldsIntegrationTests {

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

}

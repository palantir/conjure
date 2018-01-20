/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.parser.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class RequestLineDefinitionTest {

    @Test
    public void round_trip_serialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        RequestLineDefinition original = RequestLineDefinition.of("GET", PathDefinition.of("/path"));
        String string = mapper.writeValueAsString(original);

        assertThat(mapper.readValue(string, RequestLineDefinition.class))
                .isEqualTo(original);
    }
}

/*
 * (c) Copyright 2019 Palantir Technologies Inc. All rights reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableSet;
import com.palantir.conjure.defs.Conjure;
import com.palantir.conjure.spec.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class NormalizeDefinitionTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());

    @Test
    public void sort_types_services_and_errors() throws IOException {
        ConjureDefinition normalized = Conjure.parse(ImmutableSet.of(new File("src/test/resources/normalize-me.yml")));

        String actual = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(normalized);

        File file = new File("src/test/resources/normalized.conjure.json");

        if (Boolean.valueOf(System.getProperty("recreate", "false"))) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, normalized);
        }

        assertThat(file).hasContent(actual);
    }
}

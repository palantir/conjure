/*
 * (c) Copyright 2017 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.conjure.defs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.palantir.conjure.parser.ConjureDefinition;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public final class ConjureSpecTest {

    @Parameterized.Parameters(name = "{0} is valid Conjure YML: {1}")
    public static Collection<Object[]> data() {
        return getTestFiles(new File("src/test/resources/spec-tests"));
    }

    private static List<Object[]> getTestFiles(File dir) {
        List<Object[]> objects = new ArrayList<>();
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File f : files) {
            TestCaseDefinition definition;
            try {
                definition = MAPPER.readValue(f, TestCaseDefinition.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            objects.add(new Object[] {definition});
        }
        return objects;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module());

    private final TestCaseDefinition testCaseDef;

    public ConjureSpecTest(TestCaseDefinition testCaseDef) {
        this.testCaseDef = testCaseDef;
    }

    @Test
    public void testConjureSpec() throws IOException {
        // test positive cases
        testCaseDef.positive().orElse(new TreeMap<>()).entrySet().stream().forEach(entry -> {
            String testName = String.format("positive case %s", entry.getKey());
            String yml = getYmlAsString(testName, entry.getValue().conjure());
            try {
                ConjureParserUtils.parseConjureDef(MAPPER.readValue(yml, ConjureDefinition.class));
            } catch (Exception e) {
                Assertions.fail("Conjure for case should be valid according to the spec: " + testName, e);
            }
        });

        // test negative cases
        testCaseDef.negative().orElse(new TreeMap<>()).entrySet().stream().forEach(entry -> {
            String testName = String.format("negative case %s", entry.getKey());
            String yml = getYmlAsString(testName, entry.getValue().conjure());
            try {
                ConjureParserUtils.parseConjureDef(MAPPER.readValue(yml, ConjureDefinition.class));
                Assertions.fail("Conjure for case should be invalid according to the spec: " + testName);
            } catch (Exception e) {
                Assertions.assertThat(e).withFailMessage("Failure message for case did not match expectation: "
                        + testName + "\nMessage:\n" + e.getMessage() + "\ndid not contain:\n"
                        + entry.getValue().expectedError()).hasMessageContaining(entry.getValue().expectedError());
            }
        });
    }

    private static String getYmlAsString(String testName, Object obj) {
        String yml;
        try {
            yml = MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("failed to parse YML for %s", testName), e);
        }
        return yml;
    }
}

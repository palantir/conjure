/*
 * (c) Copyright 2021 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.conjure;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class KebabCasePatternTest {
    private final String[] valid = {"a-b", "a2-c3", "a-c-d23"};
    private final String[] invalid = {"", "a-b-cC", "a-2b", "a--b", "a-"};

    @Test
    public void validate() {
        SimplifiedPattern simplifiedPattern = new KebabCasePattern();
        Pattern regexPattern = Pattern.compile(simplifiedPattern.pattern());
        for (String testCase : valid) {
            assertThat(simplifiedPattern.matches(testCase))
                    .as(testCase)
                    .isTrue()
                    .isEqualTo(regexPattern.matcher(testCase).matches());
        }
        for (String testCase : invalid) {
            assertThat(simplifiedPattern.matches(testCase))
                    .as(testCase)
                    .isFalse()
                    .isEqualTo(regexPattern.matcher(testCase).matches());
        }
    }

    @Test
    public void randomSampleTest() {
        SimplifiedPattern simplifiedPattern = new KebabCasePattern();
        Pattern regexPattern = Pattern.compile(simplifiedPattern.pattern());

        PatternTestUtil.runRandomTests(
                10_000,
                8,
                ImmutableList.of('a', 'b', 'y', 'z', '-', '-', '0', '9'),
                ImmutableList.of('A', 'Z', '_'),
                testCase -> assertThat(simplifiedPattern.matches(testCase))
                        .as(testCase)
                        .isEqualTo(regexPattern.matcher(testCase).matches()));
    }
}

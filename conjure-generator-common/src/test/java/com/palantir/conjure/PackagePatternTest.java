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
import static org.quicktheories.QuickTheory.qt;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class PackagePatternTest {
    private final String[] valid = {
        "", "foo", "af.b.c", "af.b23.f2",
    };
    private final String[] invalid = {".", "a", "ad..b", "a.b.c", "ad.2.df"};

    private final PackagePattern simplifiedPattern = PackagePattern.get();
    private final Pattern regexPattern = Pattern.compile(simplifiedPattern.pattern());

    @Test
    public void validate() {
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
    void testValidCharsMatch() {
        qt().withExamples(5000)
                .forAll(Generators.stringsFromChars("abcdefghijklmnopqrstuvwxyz0123456789.", 25))
                .checkAssert(input -> {
                    assertThat(simplifiedPattern.matches(input))
                            .as(input)
                            .isEqualTo(regexPattern.matcher(input).matches());
                });
    }

    @Test
    void testInvalidCharsMatch() {
        qt().withExamples(5000)
                .forAll(Generators.stringsFromChars(
                        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.", 25))
                .checkAssert(input -> {
                    assertThat(simplifiedPattern.matches(input))
                            .as(input)
                            .isEqualTo(regexPattern.matcher(input).matches());
                });
    }
}

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.CharRange;
import net.jqwik.api.constraints.Chars;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.Test;

class EnumPatternTest {
    private final String[] valid = {
        "A", "AB2", "A_23", "A_2_J_2",
    };
    private final String[] invalid = {"", "A__A", "23A"};

    private final SimplifiedPattern simplifiedPattern = new EnumPattern();
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

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CharRange(from = 'A', to = 'Z')
    @CharRange(from = '0', to = '9')
    @Chars('_')
    @StringLength(max = 25)
    @interface ValidEnumChars {}

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @CharRange(from = 'a', to = 'z')
    @CharRange(from = 'A', to = 'Z')
    @CharRange(from = '0', to = '9')
    @Chars({'_', '-', '.'})
    @StringLength(max = 25)
    @interface InvalidEnumChars {}

    @Property(tries = 5000, seed = "8202857274439734019")
    void testValidCharsMatch(@ForAll @ValidEnumChars String input) {
        assertThat(simplifiedPattern.matches(input))
                .as(input)
                .isEqualTo(regexPattern.matcher(input).matches());
    }

    @Property(tries = 5000, seed = "8202857274439734019")
    void testInvalidCharsMatch(@ForAll @InvalidEnumChars String input) {
        assertThat(simplifiedPattern.matches(input))
                .as(input)
                .isEqualTo(regexPattern.matcher(input).matches());
    }
}

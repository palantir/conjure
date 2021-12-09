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

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PatternTestUtil {
    /** Percentage of test cases with one expected invalid char */
    private static final double INVALID_ODDS = 0.20;

    private PatternTestUtil() {}

    static void runRandomTests(
            int testRuns,
            int length,
            List<Character> validChars,
            List<Character> invalidChars,
            Consumer<String> runTest) {
        Random random = new Random(0);

        for (int i = 0; i < testRuns; i++) {
            boolean hasInvalid = random.nextDouble() < INVALID_ODDS;

            runTest.accept(IntStream.range(0, length)
                    .mapToObj(_i -> {
                        if (hasInvalid && random.nextDouble() < INVALID_ODDS) {
                            return invalidChars.get(random.nextInt(invalidChars.size()));
                        } else {
                            return validChars.get(random.nextInt(validChars.size()));
                        }
                    })
                    .map(character -> character.toString())
                    .collect(Collectors.joining()));
        }
    }
}

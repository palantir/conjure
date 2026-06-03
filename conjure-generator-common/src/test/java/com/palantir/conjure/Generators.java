/*
 * (c) Copyright 2026 Palantir Technologies Inc. All rights reserved.
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

import static org.quicktheories.generators.SourceDSL.lists;

import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;

final class Generators {

    static Gen<String> stringsFromChars(String validChars, int maxLength) {
        Gen<Character> characterGen =
                Generate.pick(validChars.chars().mapToObj(c -> (char) c).toList());
        return lists().of(characterGen).ofSizeBetween(0, maxLength).map(chars -> {
            StringBuilder sb = new StringBuilder(chars.size());
            chars.forEach(sb::append);
            return sb.toString();
        });
    }

    private Generators() {}
}

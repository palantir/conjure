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

import java.util.ArrayList;
import java.util.List;
import org.quicktheories.core.Gen;
import org.quicktheories.generators.Generate;
import org.quicktheories.generators.SourceDSL;

final class PatternTestGenerators {

    static Gen<String> stringsFromChars(String validChars, int maxLength) {
        List<Character> chars = new ArrayList<>(validChars.length());
        for (int i = 0; i < validChars.length(); i++) {
            chars.add(validChars.charAt(i));
        }
        return SourceDSL.lists()
                .of(Generate.pick(chars))
                .ofSizeBetween(0, maxLength)
                .map(list -> {
                    StringBuilder sb = new StringBuilder(list.size());
                    for (Character c : list) {
                        sb.append(c.charValue());
                    }
                    return sb.toString();
                });
    }

    private PatternTestGenerators() {}
}

/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.parsec.parsers;

import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;

public final class RawStringParser implements Parser<String> {

    private final AllowableCharacters condition;

    /**
     * Parses a "raw" string where the termination condition's default specifier accepts any string of non-whitespace
     * characters.
     */
    public RawStringParser() {
        this(NO_WHITESPACE);
    }

    /**
     * Parses a "raw" string where raw is specified by the supplied condition.
     *
     * @param condition specifies the allowable characters in this string
     */
    public RawStringParser(AllowableCharacters condition) {
        this.condition = condition;
    }

    @Override
    public String parse(ParserState input) {
        StringBuilder sb = new StringBuilder();
        int curr = input.curr();
        boolean firstChar = true;
        // not at end of the file and character is explicitly allowed
        while (curr != -1 && condition.isAllowed((char) curr)) {
            if (firstChar) {
                if (condition.notAllowedToStartWith((char) curr)) {
                    return null;
                }
                firstChar = false;
            }
            sb.append((char) curr);
            curr = input.next();
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    @Override
    public String toString() {
        return "RawStringParser(" + condition + ")";
    }

    public interface AllowableCharacters {

        boolean isAllowed(char character);

        default boolean notAllowedToStartWith(char _character) {
            return false;
        }

        String getDescription();
    }

    private static final AllowableCharacters NO_WHITESPACE = new AllowableCharacters() {
        @Override
        public boolean isAllowed(char character) {
            return !Character.isWhitespace(character);
        }

        @Override
        public String getDescription() {
            return "NO_WHITESPACE";
        }
    };
}

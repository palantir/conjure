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

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;

public final class IntegerParser implements Parser<Integer> {

    private static final Parser<String> RAW_INT_PARSER = new RawStringParser(new RawStringParser.AllowableCharacters() {
        @Override
        public boolean isAllowed(char character) {
            return Character.isDigit(character) || character == '-';
        }

        @Override
        public String getDescription() {
            return "Character.isDigit";
        }
    });

    @Override
    public Integer parse(ParserState input) throws ParseException {
        String str = RAW_INT_PARSER.parse(input);
        return str != null ? Integer.valueOf(str) : null;
    }
}

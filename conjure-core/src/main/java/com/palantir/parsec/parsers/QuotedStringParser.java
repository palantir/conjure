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

public final class QuotedStringParser implements Parser<String> {

    private final boolean respectLineBreaks;

    public QuotedStringParser() {
        this(false);
    }

    public QuotedStringParser(boolean respectLineBreaks) {
        this.respectLineBreaks = respectLineBreaks;
    }

    @Override
    public String parse(ParserState input) throws ParseException {
        StringBuilder sb = new StringBuilder();

        if (input.curr() != '"') {
            throw new ParseException("Expected a quoted string, but didn't observe a quote.", input);
        }

        int curr;
        boolean escaped = false;
        while ((curr = input.next()) != -1) {
            if (escaped) {
                escaped = false;
            } else {
                if (curr == '\\') {
                    escaped = true;
                    continue;
                } else if (curr == '"') {
                    input.next();
                    break;
                }
            }
            if (curr == -1) {
                throw new ParseException("Reached end of file while processing quoted string.", input);
            }
            if (respectLineBreaks || curr != '\n') {
                sb.append((char) curr);
            }
        }
        return sb.toString();
    }

    @Override
    public String description() {
        return "quoted string";
    }
}

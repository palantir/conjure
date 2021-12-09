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
import com.palantir.parsec.Parsers;

public final class BetweenParser<T> implements Parser<T> {

    private final Parser<?> start;
    private final Parser<?> end;
    private final Parser<T> parser;
    private final String description;

    public BetweenParser(Parser<?> start, Parser<T> parser, Parser<?> end, String description) {
        this.start = start;
        this.parser = parser;
        this.end = end;
        this.description = description;
    }

    @Override
    public T parse(ParserState input) throws ParseException {

        // First, consume the thing you expect to find at the beginning.
        // This is likely to be a string constant like "{".
        input.mark();
        if (Parsers.nullOrUnexpected(start.parse(input))) {
            input.rewind();
            throw new ParseException("Expected start token " + start.description() + " for " + description, input);
        }
        input.release();

        // Then, consume the thing you expect to find in the middle.
        // This is likely to be done with a more complicated parser
        // such as DispatchingParser.
        T item = parser.parse(input);

        if (item == null) {
            throw new ParseException("Didn't match " + parser.description() + " inside of " + description, input);
        }

        // Finally, consume the thing you expect to find at the end.
        // This is likely to be a string constant like "}".
        input.mark();
        if (Parsers.nullOrUnexpected(end.parse(input))) {
            input.rewind();
            throw new ParseException("Expected end token " + end.description() + " for " + description, input);
        }
        input.release();

        // The thing we care about was in the middle.
        // Parser<?>, and return the T.)
        return item;
    }

    @Override
    public String description() {
        return description;
    }
}

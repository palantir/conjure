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
import java.util.HashMap;
import java.util.Map;

public final class DispatchingParser<T> implements Parser<T> {

    private final String description;
    private final Map<String, Parser<T>> parserLookup;
    private final Parser<String> inputStringParser;

    public DispatchingParser(String description, Map<String, Parser<T>> parsers) {
        this(description, parsers, null);
    }

    public DispatchingParser(String description, Map<String, Parser<T>> parsers, Parser<?> whitespaceParser) {
        this(description, parsers, new RawStringParser(), whitespaceParser);
    }

    @SuppressWarnings("for-rollout:InconsistentOverloads")
    public DispatchingParser(
            String description,
            Map<String, Parser<T>> parsers,
            Parser<String> directiveParser,
            Parser<?> whitespaceParser) {
        this.description = description;
        parserLookup = new HashMap<String, Parser<T>>();
        if (whitespaceParser == null) {
            inputStringParser = directiveParser;
            parserLookup.putAll(parsers);
        } else {
            inputStringParser = Parsers.prefix(whitespaceParser, directiveParser);
            for (Map.Entry<String, Parser<T>> entry : parsers.entrySet()) {
                parserLookup.put(entry.getKey(), Parsers.prefix(whitespaceParser, entry.getValue()));
            }
        }
    }

    @Override
    public T parse(ParserState input) throws ParseException {
        T lastResult = null;
        while (input.curr() != -1) {
            String directive = Parsers.gingerly(inputStringParser).parse(input);
            if (Parsers.nullOrEmpty(directive)) {
                break;
            } else if (parserLookup.containsKey(directive)) {
                lastResult = parserLookup.get(directive).parse(input);
            } else {
                throw new ParseException("Unknown directive '" + directive + "'.", input);
            }
        }
        return lastResult;
    }

    @Override
    public String description() {
        return description;
    }
}

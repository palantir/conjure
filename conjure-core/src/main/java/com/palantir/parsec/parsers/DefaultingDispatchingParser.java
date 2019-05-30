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
import java.util.Map.Entry;

public final class DefaultingDispatchingParser<T> implements Parser<T> {

    private final Map<String, Parser<T>> map;
    private final Parser<String> inputStringParser;
    private Parser<T> defaultParser;

    public DefaultingDispatchingParser(Map<String, Parser<T>> parsers, Parser<?> whitespaceParser) {
        this(parsers, new RawStringParser(), whitespaceParser);
    }

    public DefaultingDispatchingParser(Map<String, Parser<T>> parsers, Parser<String> directiveParser,
            Parser<?> whitespaceParser) {
        this.map = new HashMap<String, Parser<T>>(parsers);
        if (whitespaceParser == null) {
            inputStringParser = directiveParser;
            this.map.putAll(parsers);
        } else {
            inputStringParser = Parsers.prefix(whitespaceParser, directiveParser);
            for (Entry<String, Parser<T>> entry : parsers.entrySet()) {
                this.map.put(entry.getKey(), Parsers.prefix(whitespaceParser, entry.getValue()));
            }
        }
    }

    /**
     * Allows for graceful handling of unknown directives. Because the default parser will need to know what the
     * directive was, it will handle the entire line.
     *
     * @param defaultParser the default parser to apply
     */
    public void setDefaultParser(Parser<T> defaultParser) {
        this.defaultParser = defaultParser;
    }

    @Override
    public T parse(ParserState input) throws ParseException {
        T lastResult = null;
        while (input.curr() != -1) {
            input.mark();
            // Mark is now before directive.

            // Gingerly will put its own mark at the exact same location.
            String directive = Parsers.gingerly(inputStringParser).parse(input);

            if (Parsers.nullOrEmpty(directive)) {
                // If we get here, we couldn't parse a directive.
                // Gingerly rewound to its mark; we should rewind too.
                input.rewind();
                break;
            } else {
                // If we get here, directive was parsed okay.

                if (map.containsKey(directive)) {
                    // Gingerly's mark is already released; release ours too.
                    input.release();
                    lastResult = map.get(directive).parse(input);

                } else if (defaultParser != null) {
                    // If we don't recognize this directive, let the default parser try it.

                    // The default parser has to handle the entire line, so
                    // rewind to before the directive.
                    input.rewind();

                    // lastResult = Parsers.gingerly(defaultParser).parse(input);
                    lastResult = defaultParser.parse(input);

                    // // Gingerly will have either released the mark or rewound to
                    // // it, so we don't need to do either. We can just handle the result.
                    // if (lastResult == null) {
                    // // We don't know what T is, so assume null is the only invalid return.
                    // return null;
                    // } else {
                    // return lastResult;
                    // }

                } else {
                    input.release();
                    throw new ParseException("Unknown directive '" + directive + "' and no default parser specified.",
                            input);
                }
            }
        }
        // If we get here, either a dispatched parser or the default parser
        // managed to run. Its result still might have been null, but that's
        // not our problem.
        return lastResult;
    }

}

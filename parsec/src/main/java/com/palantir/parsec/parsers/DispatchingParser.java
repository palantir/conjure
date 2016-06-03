/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.parsec.parsers;

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class DispatchingParser<T> implements Parser<T> {

    private final Map<String, Parser<T>> parserLookup;
    private final Parser<String> inputStringParser;

    public DispatchingParser(Map<String, Parser<T>> parsers) {
        this(parsers, null);
    }

    public DispatchingParser(Map<String, Parser<T>> parsers, Parser<?> whitespaceParser) {
        this(parsers, new RawStringParser(), whitespaceParser);
    }

    public DispatchingParser(Map<String, Parser<T>> parsers, Parser<String> directiveParser,
            Parser<?> whitespaceParser) {
        parserLookup = new HashMap<String, Parser<T>>();
        if (whitespaceParser == null) {
            inputStringParser = directiveParser;
            parserLookup.putAll(parsers);
        } else {
            inputStringParser = Parsers.prefix(whitespaceParser, directiveParser);
            for (Entry<String, Parser<T>> entry : parsers.entrySet()) {
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

}

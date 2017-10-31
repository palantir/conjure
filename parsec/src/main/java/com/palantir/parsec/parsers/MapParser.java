/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec.parsers;

import com.google.common.collect.Maps;
import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import java.util.Map;

public final class MapParser<A, B> implements Parser<Map<A, B>> {

    private final Parser<A> keyParser;
    private final Parser<B> valueParser;
    private final Parser<?> separator;

    public MapParser(Parser<A> keyParser, Parser<B> valueParser, Parser<?> separator) {
        this.keyParser = keyParser;
        this.valueParser = valueParser;
        this.separator = separator;
    }

    @Override
    public Map<A, B> parse(ParserState input) throws ParseException {
        Map<A, B> results = Maps.newHashMap();
        do {
            A key = Parsers.gingerly(keyParser).parse(input);
            if (!Parsers.nullOrUnexpected(key)) {
                B val = Parsers.gingerly(valueParser).parse(input);
                if (!Parsers.nullOrUnexpected(val)) {
                    results.put(key, val);
                } else {
                    throw new ParseException("Found key '" + key + "' without associated value.", input);
                }
            } else {
                break;
            }
        } while (Parsers.gingerly(separator).parse(input) != null);
        return results;
    }
}

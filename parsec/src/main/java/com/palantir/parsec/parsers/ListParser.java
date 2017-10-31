/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec.parsers;

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import java.util.LinkedList;
import java.util.List;

public final class ListParser<T> implements Parser<List<T>> {

    private final Parser<?> separator;
    private final Parser<T> valueParser;

    public ListParser(Parser<T> valueParser, Parser<?> separator) {
        this.valueParser = valueParser;
        this.separator = separator;
    }

    @Override
    public List<T> parse(ParserState input) throws ParseException {
        List<T> results = new LinkedList<T>();
        do {
            T val = Parsers.gingerly(valueParser).parse(input);
            if (!Parsers.nullOrUnexpected(val)) {
                results.add(val);
            } else {
                break;
            }
        } while (Parsers.gingerly(separator).parse(input) != null);
        return results;
    }

}

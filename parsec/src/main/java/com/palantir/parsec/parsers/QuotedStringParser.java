/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
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

}

/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
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

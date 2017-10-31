/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec.parsers;

import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;

public final class RawStringParser implements Parser<String> {

    private final AllowableCharacters condition;

    /**
     * Parses a "raw" string where the termination condition's default specifier accepts any string of non-whitespace
     * characters.
     */
    public RawStringParser() {
        this(NO_WHITESPACE);
    }

    /**
     * Parses a "raw" string where raw is specified by the supplied condition.
     *
     * @param condition specifies the allowable characters in this string
     */
    public RawStringParser(AllowableCharacters condition) {
        this.condition = condition;
    }

    @Override
    public String parse(ParserState input) {
        StringBuilder sb = new StringBuilder();
        int curr = input.curr();
        // not at end of the file and character is explicitly allowed
        while (curr != -1 && condition.isAllowed((char) curr)) {
            sb.append((char) curr);
            curr = input.next();
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    @Override
    public String toString() {
        return "RawStringParser(" + condition + ")";
    }

    public interface AllowableCharacters {
        boolean isAllowed(char character);

        String getDescription();
    }

    private static final AllowableCharacters NO_WHITESPACE = new AllowableCharacters() {
        @Override
        public boolean isAllowed(char character) {
            return !Character.isWhitespace(character);
        }

        @Override
        public String getDescription() {
            return "NO_WHITESPACE";
        }
    };

}

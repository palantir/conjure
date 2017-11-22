/*
 * (c) Copyright 2016 Palantir Technologies Inc. All rights reserved.
 */

package com.palantir.parsec;

import com.palantir.parsec.parsers.BetweenParser;
import com.palantir.parsec.parsers.ExpectantParser;
import com.palantir.parsec.parsers.ExpectationResult;
import com.palantir.parsec.parsers.RawStringParser;

public final class Parsers {

    private Parsers() {}

    private static final Parser<String> WHITESPACE = new RawStringParser(new RawStringParser.AllowableCharacters() {
        @Override
        public boolean isAllowed(char character) {
            return Character.isWhitespace(character);
        }

        @Override
        public String getDescription() {
            return "Character.isWhitespace";
        }
    });

    /**
     * Runs the prefix parser, followed by the `parser`, returning result of the latter.
     *
     * @param <T> the type the target parser returns
     * @param prefix a parser to consume some prefix
     * @param parser the target parser to run
     * @return the specified parser
     */
    public static <T> Parser<T> prefix(final Parser<?> prefix, final Parser<T> parser) {
        return new Parser<T>() {
            @Override
            public T parse(ParserState input) throws ParseException {
                prefix.parse(input);
                return parser.parse(input);
            }

            @Override
            public String toString() {
                return prefix.toString() + " then " + parser.toString();
            }
        };
    }

    /**
     * Returns a whitespace parser.
     *
     * @return canonical whitespace parser
     */
    public static Parser<String> whitespace() {
        return WHITESPACE;
    }

    /**
     * Runs the whitespace parser followed by `parser`, returning result of the latter.
     *
     * @param <T> the type the target parser returns
     * @param parser the target parser to run
     * @return the specified parser
     */
    public static <T> Parser<T> whitespace(Parser<T> parser) {
        return prefix(whitespace(), parser);
    }

    /**
     * Attempts to parse using parser; if the parser returns null or is a expectant parser and returns false, then the
     * CompilerInput state is reset. Otherwise, the state remains unchanged.
     *
     * @param <T> the type the target parser returns
     * @param parser the target parser to run
     * @return the specified parser
     */
    public static <T> Parser<T> gingerly(final Parser<T> parser) {
        return new Parser<T>() {
            @Override
            public T parse(ParserState input) throws ParseException {
                input.mark();
                T result = parser.parse(input);

                if (!nullOrUnexpected(result)) {
                    input.release();
                } else {
                    input.rewind();
                }
                return result;
            }
        };
    }

    /**
     * Parse either firstOption or iterate through otherOptions and return result.
     *
     * @param <T> the type the parsers return
     * @param firstOption the first parser to try
     * @param otherOptions additional parsers to try
     * @return the specified parser
     */
    @SafeVarargs
    public static <T> Parser<T> or(final Parser<? extends T> firstOption, final Parser<? extends T>... otherOptions) {
        return new Parser<T>() {
            @Override
            public T parse(ParserState input) throws ParseException {
                T result = gingerly(firstOption).parse(input);
                if (result != null) {
                    return result;
                }
                for (Parser<? extends T> nextOption : otherOptions) {
                    result = gingerly(nextOption).parse(input);
                    if (result != null) {
                        return result;
                    }
                }
                return result;
            }
        };
    }

    public static ExpectantParser expect(String expectation) {
        return new ExpectantParser(expectation);
    }

    public static <T> BetweenParser<T> between(ExpectantParser start, Parser<T> parser, ExpectantParser end) {
        return new BetweenParser<T>(start, parser, end);
    }

    public static <T> BetweenParser<T> between(String start, Parser<T> parser, String end) {
        return new BetweenParser<T>(expect(start), parser, expect(end));
    }

    public static <T> BetweenParser<T> liberalBetween(String start, Parser<T> parser, String end) {
        return new BetweenParser<T>(whitespace(expect(start)), whitespace(parser), whitespace(expect(end)));
    }

    public static <T> Parser<T> eof(Parser<T> parser) {
        return new Parser<T>() {
            @Override
            public T parse(ParserState input) throws ParseException {
                T result = parser.parse(input);

                if (input.next() != -1) {
                    return null;
                }
                return result;
            }
        };
    }

    /**
     * Returns true when the argument is null or is equal to {@link ExpectationResult#INCORRECT}.
     *
     * @param obj object to compare
     * @return true when the argument is null or is equal to {@link ExpectationResult#INCORRECT}
     */
    public static boolean nullOrUnexpected(Object obj) {
        return obj == null || ExpectationResult.INCORRECT.equals(obj);
    }

    /**
     * Returns true when the argument is null or is a String and is empty.
     *
     * @param string the string to test
     * @return true when {@code string} is null or empty
     */
    public static boolean nullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

}

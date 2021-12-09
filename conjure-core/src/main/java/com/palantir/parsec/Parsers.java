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
            public String description() {
                return parser.description();
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

            @Override
            public String description() {
                return parser.description();
            }
        };
    }

    /**
     * Parse either firstOption or iterate through otherOptions and return result.
     *
     * Exceptions are caught while trying options - if no options succeed, then the last exception thrown is re-thrown
     * as a {@code ParseError}.
     *
     * @param <T> the type the parsers return
     * @param firstOption the first parser to try
     * @param otherOptions additional parsers to try
     * @return the specified parser
     */
    @SafeVarargs
    public static <T> Parser<T> or(
            final String description,
            final Parser<? extends T> firstOption,
            final Parser<? extends T>... otherOptions) {
        return new Parser<T>() {
            @Override
            public T parse(ParserState input) throws ParseException {

                T result;
                Exception exception = null;

                input.mark();
                try {
                    result = gingerly(firstOption).parse(input);
                } catch (RuntimeException e) {
                    exception = e;
                    input.rewind();
                    result = null;
                }

                if (result != null) {
                    return result;
                }
                for (Parser<? extends T> nextOption : otherOptions) {
                    input.mark();
                    try {
                        result = gingerly(nextOption).parse(input);
                    } catch (RuntimeException e) {
                        if (exception == null) {
                            exception = e;
                        } else {
                            exception.addSuppressed(e);
                        }
                        input.rewind();
                    }

                    if (result != null) {
                        return result;
                    }
                }

                if (exception != null) {
                    throw new ParseException(exception.getMessage(), input, exception);
                }

                return null;
            }

            @Override
            public String description() {
                return description;
            }
        };
    }

    public static ExpectantParser expect(String expectation) {
        return new ExpectantParser(expectation);
    }

    public static <T> BetweenParser<T> between(
            ExpectantParser start, Parser<T> parser, ExpectantParser end, String description) {
        return new BetweenParser<T>(start, parser, end, description);
    }

    public static <T> BetweenParser<T> between(String start, Parser<T> parser, String end, String description) {
        return new BetweenParser<T>(expect(start), parser, expect(end), description);
    }

    public static <T> BetweenParser<T> liberalBetween(String start, Parser<T> parser, String end, String description) {
        return new BetweenParser<T>(
                whitespace(expect(start)), whitespace(parser), whitespace(expect(end)), description);
    }

    public static <T> Parser<T> eof(Parser<T> parser) {
        return new Parser<T>() {
            @Override
            public T parse(ParserState input) throws ParseException {
                T result = parser.parse(input);

                if (input.curr() != -1) {
                    return null;
                }
                return result;
            }

            @Override
            public String description() {
                return "end-of-file";
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

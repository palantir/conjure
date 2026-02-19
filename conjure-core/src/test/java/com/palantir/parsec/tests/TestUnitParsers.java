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

package com.palantir.parsec.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
import com.palantir.parsec.ParserState;
import com.palantir.parsec.Parsers;
import com.palantir.parsec.StringParserState;
import com.palantir.parsec.parsers.BetweenParser;
import com.palantir.parsec.parsers.BooleanParser;
import com.palantir.parsec.parsers.DispatchingParser;
import com.palantir.parsec.parsers.ExpectantParser;
import com.palantir.parsec.parsers.ExpectationResult;
import com.palantir.parsec.parsers.ListParser;
import com.palantir.parsec.parsers.MapParser;
import com.palantir.parsec.parsers.QuotedStringParser;
import com.palantir.parsec.parsers.RawStringParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public final class TestUnitParsers {

    @Test
    public void testQuotedStringParser() throws ParseException {
        assertThat(new QuotedStringParser(false).parse(new StringParserState("\"Test 123\"")))
                .isEqualTo("Test 123");
        assertThat(new QuotedStringParser(false).parse(new StringParserState("\"Test \\\"123\"")))
                .isEqualTo("Test \"123");
        assertThat(new QuotedStringParser(false).parse(new StringParserState("\"Test \n\n123\"")))
                .isEqualTo("Test 123");
        assertThat(new QuotedStringParser(true).parse(new StringParserState("\"Test \n\n123\"")))
                .isEqualTo("Test \n\n123");
    }

    @Test
    public void testRawStringParser() {
        assertThat(new RawStringParser().parse(new StringParserState("a:b123"))).isEqualTo("a:b123");
        assertThat(new RawStringParser().parse(new StringParserState(" a:b123")))
                .isNull();
        assertThat(new RawStringParser().parse(new StringParserState("ab123 "))).isEqualTo("ab123");
        assertThat(new RawStringParser().parse(new StringParserState("ab123\n")))
                .isEqualTo("ab123");
    }

    @Test
    public void testDispatchingParser() throws ParseException {
        Map<String, Parser<String>> map = new HashMap<String, Parser<String>>();

        map.put("dir1", new RawStringParser());
        map.put("dir2", new QuotedStringParser());

        assertThat(new DispatchingParser<String>("dispatch", map, Parsers.whitespace())
                        .parse(new StringParserState("dir1 abc\ndir2 \"def\n\"\n")))
                .isEqualTo("def");
        assertThat(new DispatchingParser<String>("dispatch", map, Parsers.whitespace())
                        .parse(new StringParserState("dir1 abc\ndir2 \"def\n\"\n")))
                .isEqualTo("def");
    }

    @Test
    public void testExpectantParser() {
        assertThat(new ExpectantParser("abcdef").parse(new StringParserState("abcdef")))
                .isEqualTo(ExpectationResult.CORRECT);
        assertThat(new ExpectantParser("abcdef").parse(new StringParserState("abcde")))
                .isEqualTo(ExpectationResult.INCORRECT);
        assertThat(new ExpectantParser("abcdef").parse(new StringParserState("abcdeg")))
                .isEqualTo(ExpectationResult.INCORRECT);
    }

    @Test
    public void testBetweenParser() throws ParseException {
        assertThat(new BetweenParser<String>(
                                new ExpectantParser("{"),
                                Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                                Parsers.prefix(Parsers.whitespace(), new ExpectantParser("}")),
                                "brackets")
                        .parse(new StringParserState("{ abcdef }")))
                .isEqualTo("abcdef");
    }

    @Test
    public void testListParser() throws ParseException {
        List<String> list = new ArrayList<>();
        list.add("abc");
        list.add("def");
        assertThat(new ListParser<>(new QuotedStringParser(), Parsers.whitespace())
                        .parse(new StringParserState("\"abc\" \"def\"")))
                .containsExactlyElementsOf(list);
    }

    @Test
    public void testMapParser() throws ParseException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("abc", "def");
        assertThat(new MapParser<String, String>(
                                new RawStringParser(),
                                Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                                Parsers.whitespace())
                        .parse(new StringParserState("abc def")))
                .containsExactlyInAnyOrderEntriesOf(map);
        assertThat(new MapParser<String, String>(
                                Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                                Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                                Parsers.whitespace())
                        .parse(new StringParserState(" abc def")))
                .containsExactlyInAnyOrderEntriesOf(map);
        map.put("ghi", "jkl");
        assertThat(new MapParser<String, String>(
                                Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                                Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                                Parsers.whitespace())
                        .parse(new StringParserState("abc def  ghi jkl")))
                .containsExactlyInAnyOrderEntriesOf(map);
    }

    @Test
    public void testMapParserBuiltup() throws ParseException {

        Map<String, String> map = new HashMap<String, String>();
        map.put("abc", "def");

        RawStringParser noWhitespaceStringParser = new RawStringParser();
        Parser<String> whitespaceStringParser = Parsers.prefix(Parsers.whitespace(), new RawStringParser());

        MapParser<String, String> mapParser1 =
                new MapParser<String, String>(noWhitespaceStringParser, whitespaceStringParser, Parsers.whitespace());
        MapParser<String, String> mapParser2 =
                new MapParser<String, String>(whitespaceStringParser, whitespaceStringParser, Parsers.whitespace());

        assertThat(mapParser1.parse(new StringParserState("abc def"))).containsExactlyInAnyOrderEntriesOf(map);
        assertThat(mapParser2.parse(new StringParserState(" abc def"))).containsExactlyInAnyOrderEntriesOf(map);

        map.put("ghi", "jkl");

        assertThat(mapParser2.parse(new StringParserState("abc def  ghi jkl"))).containsExactlyInAnyOrderEntriesOf(map);
    }

    @Test
    public void testOrParser() throws ParseException {
        Parser<String> rawNoPunctuation = new RawStringParser(new RawStringParser.AllowableCharacters() {
            @Override
            public boolean isAllowed(char character) {
                return Character.isLetterOrDigit(character);
            }

            @Override
            public String getDescription() {
                return "Character.isLetterOrDigit";
            }
        });

        assertThat(Parsers.or("or", rawNoPunctuation, new QuotedStringParser()).parse(new StringParserState("abcdef")))
                .isEqualTo("abcdef");
        assertThat(Parsers.or("or", rawNoPunctuation, new QuotedStringParser())
                        .parse(new StringParserState("\"abcdef" + "\"")))
                .isEqualTo("abcdef");
    }

    @SuppressWarnings("for-rollout:StringConcatToTextBlock")
    @Test
    public void testOrParserErrorPassthrough() throws ParseException {
        Parser<String> alwaysThrows = new Parser<String>() {
            @Override
            public String parse(ParserState input) throws ParseException {
                input.next();
                throw new IllegalStateException("bad thing");
            }

            @Override
            public String description() {
                return "always throws";
            }
        };

        assertThat(Parsers.or("or", alwaysThrows, alwaysThrows, new QuotedStringParser())
                        .parse(new StringParserState("\"abcdef\"")))
                .isEqualTo("abcdef");
        assertThatThrownBy(() ->
                        Parsers.or("or", alwaysThrows, alwaysThrows).parse(new StringParserState("\"abcdef" + "\"")))
                .isInstanceOf(ParseException.class)
                .hasMessage("bad thing\n" + "\"abcdef\"\n" + "^")
                .hasCause(new IllegalStateException("bad thing"))
                .cause()
                .hasSuppressedException(new IllegalStateException("bad thing"));
    }

    private static final Parser<String> rsp = Parsers.prefix(Parsers.whitespace(), new RawStringParser());
    private static final Parser<Boolean> bp = Parsers.prefix(Parsers.whitespace(), new BooleanParser());

    @Test
    public void testGingerlyBooleanFalse() throws ParseException {
        StringParserState state = new StringParserState("false 123");

        assertThat(Parsers.gingerly(bp).parse(state)).isFalse();
        assertThat(rsp.parse(state)).isEqualTo("123");
    }

    @Test
    public void testGingerlyBooleanTrue() throws ParseException {
        StringParserState state = new StringParserState("true 123");

        assertThat(Parsers.gingerly(bp).parse(state)).isTrue();
        assertThat(rsp.parse(state)).isEqualTo("123");
    }

    @Test
    public void testEofParser() throws ParseException {
        assertThat(Parsers.eof(new ExpectantParser("abc")).parse(new StringParserState("abc")))
                .isEqualTo(ExpectationResult.CORRECT);
    }

    @Test
    public void testEofParserFails() throws ParseException {
        assertThat(Parsers.eof(new ExpectantParser("abc")).parse(new StringParserState("abcd")))
                .isNull();
    }
}

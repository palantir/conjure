/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.parsec.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.palantir.parsec.ParseException;
import com.palantir.parsec.Parser;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public final class TestUnitParsers {

    @Test
    public void testQuotedStringParser() throws ParseException {
        assertEquals("Test 123", new QuotedStringParser(false).parse(new StringParserState("\"Test 123\"")));
        assertEquals("Test \"123",
                new QuotedStringParser(false).parse(new StringParserState("\"Test \\\"123\"")));
        assertEquals("Test 123", new QuotedStringParser(false).parse(new StringParserState("\"Test \n\n123\"")));
        assertEquals("Test \n\n123",
                new QuotedStringParser(true).parse(new StringParserState("\"Test \n\n123\"")));
    }

    @Test
    public void testRawStringParser() {
        assertEquals("a:b123", new RawStringParser().parse(new StringParserState("a:b123")));
        assertNull(new RawStringParser().parse(new StringParserState(" a:b123")));
        assertEquals("ab123", new RawStringParser().parse(new StringParserState("ab123 ")));
        assertEquals("ab123", new RawStringParser().parse(new StringParserState("ab123\n")));
    }

    @Test
    public void testDispatchingParser() throws ParseException {
        Map<String, Parser<String>> map = new HashMap<String, Parser<String>>();

        map.put("dir1", new RawStringParser());
        map.put("dir2", new QuotedStringParser());

        assertEquals("def", new DispatchingParser<String>(map, Parsers.whitespace())
                .parse(new StringParserState("dir1 abc\ndir2 \"def\n\"\n")));
        assertEquals("def", new DispatchingParser<String>(map, Parsers.whitespace())
                .parse(new StringParserState("dir1 abc\ndir2 \"def\n\"\n")));
    }

    @Test
    public void testExpectantParser() {
        assertEquals(ExpectationResult.CORRECT,
                new ExpectantParser("abcdef").parse(new StringParserState("abcdef")));
        assertEquals(ExpectationResult.INCORRECT,
                new ExpectantParser("abcdef").parse(new StringParserState("abcde")));
        assertEquals(ExpectationResult.INCORRECT,
                new ExpectantParser("abcdef").parse(new StringParserState("abcdeg")));
    }

    @Test
    public void testBetweenParser() throws ParseException {
        assertEquals("abcdef",
                new BetweenParser<String>(new ExpectantParser("{"),
                        Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                        Parsers.prefix(Parsers.whitespace(), new ExpectantParser("}")))
                        .parse(new StringParserState("{ abcdef }")));
    }

    @Test
    public void testListParser() throws ParseException {
        List<String> list = new LinkedList<String>();
        list.add("abc");
        list.add("def");
        assertEquals(list, new ListParser<String>(new QuotedStringParser(), Parsers.whitespace())
                .parse(new StringParserState("\"abc\" \"def\"")));
    }

    @Test
    public void testMapParser() throws ParseException {
        Map<String, String> map = new HashMap<String, String>();
        map.put("abc", "def");
        assertEquals(map, new MapParser<String, String>(new RawStringParser(),
                Parsers.prefix(Parsers.whitespace(), new RawStringParser()), Parsers.whitespace()).parse(
                new StringParserState("abc def")));
        assertEquals(map, new MapParser<String, String>(Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                Parsers.prefix(Parsers.whitespace(), new RawStringParser()), Parsers.whitespace()).parse(
                new StringParserState(" abc def")));
        map.put("ghi", "jkl");
        assertEquals(map, new MapParser<String, String>(Parsers.prefix(Parsers.whitespace(), new RawStringParser()),
                Parsers.prefix(Parsers.whitespace(), new RawStringParser()), Parsers.whitespace()).parse(
                new StringParserState("abc def  ghi jkl")));
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

        assertEquals(map, mapParser1.parse(new StringParserState("abc def")));
        assertEquals(map, mapParser2.parse(new StringParserState(" abc def")));

        map.put("ghi", "jkl");

        assertEquals(map, mapParser2.parse(new StringParserState("abc def  ghi jkl")));
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

        assertEquals("abcdef",
                Parsers.or(rawNoPunctuation, new QuotedStringParser()).parse(new StringParserState("abcdef")));
        assertEquals("abcdef",
                Parsers.or(rawNoPunctuation, new QuotedStringParser()).parse(new StringParserState("\"abcdef\"")));
    }

    private static final Parser<String> rsp = Parsers.prefix(Parsers.whitespace(), new RawStringParser());
    private static final Parser<Boolean> bp = Parsers.prefix(Parsers.whitespace(), new BooleanParser());

    @Test
    public void testGingerlyBooleanFalse() throws ParseException {
        StringParserState state = new StringParserState("false 123");

        assertEquals(Boolean.FALSE, Parsers.gingerly(bp).parse(state));
        assertEquals("123", rsp.parse(state));
    }

    @Test
    public void testGingerlyBooleanTrue() throws ParseException {
        StringParserState state = new StringParserState("true 123");

        assertEquals(Boolean.TRUE, Parsers.gingerly(bp).parse(state));
        assertEquals("123", rsp.parse(state));
    }

}
